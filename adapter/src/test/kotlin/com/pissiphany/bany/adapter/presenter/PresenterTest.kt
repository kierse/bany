package com.pissiphany.bany.adapter.presenter

import com.pissiphany.bany.adapter.dataStructure.ViewModel
import com.pissiphany.bany.domain.dataStructure.Account
import com.pissiphany.bany.domain.dataStructure.Budget
import com.pissiphany.bany.domain.dataStructure.SyncTransactionsResult
import com.pissiphany.bany.domain.dataStructure.Transaction
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertAll
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

internal class PresenterTest {
    private lateinit var budget: Budget
    private lateinit var account: Account
    private lateinit var transaction: Transaction
    private lateinit var date: LocalDate
    private lateinit var results: List<SyncTransactionsResult>
    private lateinit var view: TestView
    private lateinit var model: ViewModel

    @BeforeEach
    fun setup() {
        budget = Budget(id = "budgetId", name = "budgetName")
        account = Account(id = "accountId", name = "name", balance = 2L, type = Account.Type.CHECKING, closed = false)
        date = LocalDate.EPOCH
        transaction = Transaction(id = "transactionId", date = date.plusDays(1), amount = 3L)
        results = listOf(SyncTransactionsResult(
            budget, account, date, listOf(transaction)
        ))
        view = TestView()

        Presenter(view).present(results)

        model = view.model ?: fail("no model!")
    }

    @Test
    fun present__budgetId() {
        assertTrue(model.records[0].budgetId.contains(budget.id))
    }

    @Test
    fun present__budgetName() {
        assertTrue(model.records[0].budgetName.contains(budget.name))
    }

    @Test
    fun present__accountId() {
        assertTrue(model.records[0].accountId.contains(account.id))
    }

    @Test
    fun present__accountName() {
        assertTrue(model.records[0].accountName.contains(account.name))
    }

    @Test
    fun present__latest_transaction_date() {
        assertTrue(model.records[0].latestTransactionDate.contains(date.format(DateTimeFormatter.ISO_LOCAL_DATE)))
    }

    @Test
    fun present__no_latest_transaction_date() {
        results = listOf(SyncTransactionsResult(
            budget, account, null, listOf(transaction)
        ))

        Presenter(view).present(results)

        model = view.model ?: fail("no model!")
        assertTrue(model.records[0].latestTransactionDate.isEmpty())
    }

    @Test
    fun present__transactions() {
        assertAll("transactions",
            { assertTrue(model.records[0].transactions[0].contains(transaction.id)) },
            { assertTrue(model.records[0].transactions[0].contains(transaction.date.format(DateTimeFormatter.ISO_LOCAL_DATE))) },
            {
                val bigDecimal = BigDecimal(transaction.amount).movePointLeft(3)
                assertTrue(model.records[0].transactions[0].contains(bigDecimal.toString()))
            }
        )
    }

    @Test
    fun present__no_transactions() {
        results = listOf(SyncTransactionsResult(
            budget, account, date, listOf()
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