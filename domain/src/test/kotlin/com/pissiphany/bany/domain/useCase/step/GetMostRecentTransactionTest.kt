package com.pissiphany.bany.domain.useCase.step

import com.pissiphany.bany.domain.dataStructure.Account
import com.pissiphany.bany.domain.dataStructure.Budget
import com.pissiphany.bany.domain.dataStructure.Transaction
import com.pissiphany.bany.domain.dataStructure.UpdatedTransactions
import com.pissiphany.bany.domain.gateway.YnabMostRecentTransactionsGateway
import com.pissiphany.bany.domain.repository.YnabLastKnowledgeOfServerRepository
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.time.LocalDate
import java.time.OffsetDateTime

internal class GetMostRecentTransactionTest {

    @Test
    fun getTransaction() {
        val budget = Budget("budgetId", "name")
        val account = Account("accountId", "name", 1, false, Account.Type.CHECKING)

        val transactions = listOf(
            Transaction("transactionId1", OffsetDateTime.now(), "payee", "memo", 10),
            Transaction("transactionId2", OffsetDateTime.now(), "payee", "memo", 15)
        )

        val cache = TestRepo()
        val gateway = TestGateway(transactions)
        val step = GetMostRecentTransaction(cache, gateway)

        assertEquals(transactions.first(), step.getTransaction(budget, account))
    }

    private class TestRepo : YnabLastKnowledgeOfServerRepository {
        var lastKnowledge = 1
            private set

        override fun getLastKnowledgeOfServer(account: Account): Int {
            return if (account.id == "accountId") lastKnowledge else 0
        }

        override fun saveLastKnowledgeOfServer(account: Account, lastKnowledgeOfServer: Int) {
            lastKnowledge = lastKnowledgeOfServer
        }
    }

    private class TestGateway(private val transactions: List<Transaction>) : YnabMostRecentTransactionsGateway {
        override fun getUpdatedTransactions(
            budget: Budget, account: Account, lastKnowledgeOfServer: Int
        ): UpdatedTransactions {
            if (lastKnowledgeOfServer > 0) {
                return UpdatedTransactions(transactions, lastKnowledgeOfServer + 1)
            }

            throw IllegalStateException("shouldn't be here!")
        }
    }
}