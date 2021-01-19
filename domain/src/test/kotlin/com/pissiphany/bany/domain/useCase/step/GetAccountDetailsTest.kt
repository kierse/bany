package com.pissiphany.bany.domain.useCase.step

import com.pissiphany.bany.domain.dataStructure.*
import com.pissiphany.bany.domain.gateway.YnabAccountDetailsGateway
import com.pissiphany.bany.domain.repository.YnabLastKnowledgeOfServerRepository
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.time.OffsetDateTime

internal class GetAccountDetailsTest {

    @Test
    fun getTransaction() {
        val budgetAccountIds = BudgetAccountIds(name = "name", budgetId = "budgetId", accountId = "accountId")

        val transactions = listOf(
            AccountTransaction("transactionId1", OffsetDateTime.now(), "payee", "memo", 10),
            AccountTransaction("transactionId2", OffsetDateTime.now(), "payee", "memo", 15)
        )

        val cache = TestRepo()
        val gateway = TestGateway(transactions)
        val step = GetAccountDetails(cache, gateway)

        assertEquals(transactions.first(), step.getAccountAndLastTransaction(budgetAccountIds))
    }

    private class TestRepo : YnabLastKnowledgeOfServerRepository {
        var lastKnowledge = 1
            private set

        override fun getLastKnowledgeOfServer(budgetAccountIds: BudgetAccountIds): Int {
            return if (budgetAccountIds.accountId == "accountId") lastKnowledge else 0
        }

        override fun saveLastKnowledgeOfServer(budgetAccountIds: BudgetAccountIds, lastKnowledgeOfServer: Int) {
            lastKnowledge = lastKnowledgeOfServer
        }
    }

    private class TestGateway(private val transactions: List<AccountTransaction>) : YnabAccountDetailsGateway {
        override fun getAccount(budgetAccountIds: BudgetAccountIds): Account? {
            TODO("Not yet implemented")
        }

        override fun getUpdatedTransactions(
            budgetAccountIds: BudgetAccountIds,
            lastKnowledgeOfServer: Int
        ): UpdatedTransactions {
            if (lastKnowledgeOfServer > 0) {
                return UpdatedTransactions(transactions, lastKnowledgeOfServer + 1)
            }

            throw IllegalStateException("shouldn't be here!")
        }

    }
}