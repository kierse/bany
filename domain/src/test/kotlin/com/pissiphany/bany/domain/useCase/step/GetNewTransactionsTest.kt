package com.pissiphany.bany.domain.useCase.step

import com.pissiphany.bany.domain.dataStructure.Account
import com.pissiphany.bany.domain.dataStructure.Budget
import com.pissiphany.bany.domain.dataStructure.Transaction
import com.pissiphany.bany.domain.gateway.ThirdPartyTransactionGateway
import com.pissiphany.bany.domain.gateway.ThirdPartyTransactionGatewayFactory
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.time.LocalDate

internal class GetNewTransactionsTest {
    @Test
    fun getTransactions() {
        val date = LocalDate.now()
        val account = Account("accountId", "name", 1L, false, Account.Type.CHECKING)
        val transactions = listOf(Transaction("transactionId", date, 2L))
        val transactionGateways = listOf(TestGateway(account.id, transactions, date))
        val gatewayFactory = TestGatewayFactory()

        val step = GetNewTransactions(gatewayFactory)

        assertIterableEquals(transactions, step.getTransactions(account, date))
    }

    @Test
    fun getTransactions__account_not_found() {
        val date = LocalDate.now()
        val account = Account("accountId", "name", 1L, false, Account.Type.CHECKING)
        val transactions = listOf(Transaction("transactionId", date, 2L))
        val transactionGateways = listOf(TestGateway(account.id, transactions, date))

        val step = GetNewTransactions(transactionGateways)

        assertTrue(step.getTransactions(account.copy("differentId"), date).isEmpty())
    }

    private class TestGatewayFactory : ThirdPartyTransactionGatewayFactory {
        override fun getGateway(budget: Budget, account: Account): ThirdPartyTransactionGateway {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }

    private class TestGateway(
        private val transactions: List<Transaction>, private val date: LocalDate
    ) : ThirdPartyTransactionGateway {
        override fun getNewTransactionSince(date: LocalDate?): List<Transaction> {
            return if (this.date == date) transactions else emptyList()
        }
    }
}