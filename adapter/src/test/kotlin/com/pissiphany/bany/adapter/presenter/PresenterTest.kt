package com.pissiphany.bany.adapter.presenter

import com.pissiphany.bany.adapter.dataStructure.ViewModel
import com.pissiphany.bany.domain.dataStructure.*
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertAll
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

internal class PresenterTest {
    private lateinit var budgetAccountIds: BudgetAccountIds
    private lateinit var transaction: Transaction
    private lateinit var date: OffsetDateTime
    private lateinit var results: List<SyncTransactionsResult>
    private lateinit var view: TestView
    private lateinit var model: ViewModel

    @BeforeEach
    fun setup() {
        budgetAccountIds = BudgetAccountIds(name = "name", budgetId = "budgetId", accountId = "accountId")

        date = OffsetDateTime.of(1970,1,1,0,0,0,0, ZoneOffset.UTC)

        transaction = AccountTransaction(
            id = "transactionId",
            date = date.plusDays(1),
            amountInCents = 300,
            payee = "payee",
            memo = "memo"
        )
        results = listOf(SyncTransactionsResult(
            budgetAccountIds, date, listOf(transaction)
        ))
        view = TestView()

        Presenter(view).present(results)

        model = view.model ?: fail("no model!")
    }

    @Test
    fun present__budgetId() {
        assertTrue(model.records[0].budgetId.contains(budgetAccountIds.budgetId))
    }

    @Test
    fun present__accountId() {
        assertTrue(model.records[0].accountId.contains(budgetAccountIds.accountId))
    }

    @Test
    fun present__accountName() {
        assertTrue(model.records[0].accountName.contains(budgetAccountIds.name))
    }

    @Test
    fun present__latest_transaction_date() {
        assertTrue(model.records[0].latestTransactionDate.contains(date.format(DateTimeFormatter.ISO_LOCAL_DATE)))
    }

    @Test
    fun present__no_latest_transaction_date() {
        results = listOf(SyncTransactionsResult(
            budgetAccountIds, null, listOf(transaction)
        ))

        Presenter(view).present(results)

        model = view.model ?: fail("no model!")
        assertTrue(model.records[0].latestTransactionDate.isEmpty())
    }

    @Test
    fun present__transactions() {
        assertAll("transactions",
            // TODO test AccountBalance transactions
            { assertTrue(model.records[0].transactions[0].contains(transaction.date.format(DateTimeFormatter.ISO_LOCAL_DATE))) },
            {
                val bigDecimal = BigDecimal(transaction.amountInCents).movePointLeft(2)
                assertTrue(model.records[0].transactions[0].contains(bigDecimal.toString()))
            }
        )
    }

    @Test
    fun present__no_transactions() {
        results = listOf(SyncTransactionsResult(
            budgetAccountIds, date, listOf()
        ))

        Presenter(view).present(results)

        model = view.model ?: fail("no model!")
        assertTrue(model.records[0].transactions.isEmpty())
    }

    private class TestView : Presenter.View {
        var model: ViewModel? = null
        override fun display(model: ViewModel) {
            this.model = model
        }
    }
}