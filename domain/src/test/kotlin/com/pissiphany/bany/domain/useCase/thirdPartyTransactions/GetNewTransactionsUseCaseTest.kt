package com.pissiphany.bany.domain.useCase.thirdPartyTransactions

import com.pissiphany.bany.domain.dataStructure.Account
import com.pissiphany.bany.domain.dataStructure.Transaction
import com.pissiphany.bany.domain.gateway.ThirdPartyTransactionGateway
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.time.LocalDate
import java.time.LocalTime

internal class GetNewTransactionsUseCaseTest {
    @Test
    fun run__success() {
        val date = LocalDate.now()
        val account = Account("accountId", "name", 1L, false, Account.Type.CHECKING)
        val transactions = listOf(Transaction("transactionId", date, 2L))
        val transactionGateways = listOf(TestGateway(account, transactions, date))

        val input = InputBoundary(account, date)
        val output = OutputBoundary(transactions)

        val uc = GetNewTransactionsUseCase(transactionGateways)

        uc.run(input, output)

        assertIterableEquals(transactions, output.transactions)
    }

    @Test
    fun run__account_not_found() {
        val date = LocalDate.now()
        val account = Account("accountId", "name", 1L, false, Account.Type.CHECKING)
        val transactions = listOf(Transaction("transactionId", date, 2L))
        val transactionGateways = listOf(TestGateway(account, transactions, date))

        val input = InputBoundary(Account("id", "name", 2L, true, Account.Type.CREDIT_CARD), date)
        val output = OutputBoundary()

        val uc = GetNewTransactionsUseCase(transactionGateways)

        uc.run(input, output)

        assertTrue(output.transactions.isEmpty())
    }

    private class InputBoundary(
        override val account: Account, override val date: LocalDate?
    ) : GetNewTransactionsInputBoundary
    private class OutputBoundary(
        override var transactions: List<Transaction> = emptyList()
    ) : GetNewTransactionsOutputBoundary

    private class TestGateway(
        override val account: Account, private val transactions: List<Transaction>, private val date: LocalDate
    ) : ThirdPartyTransactionGateway {
        override fun getNewTransactionSince(date: LocalDate?): List<Transaction> {
            return if (this.date == date) transactions else emptyList()
        }
    }
}