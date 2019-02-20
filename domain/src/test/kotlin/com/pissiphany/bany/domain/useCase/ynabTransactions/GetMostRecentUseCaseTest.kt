package com.pissiphany.bany.domain.useCase.ynabTransactions

import com.pissiphany.bany.domain.dataStructure.Account
import com.pissiphany.bany.domain.dataStructure.Transaction
import com.pissiphany.bany.domain.dataStructure.UpdatedTransactions
import com.pissiphany.bany.domain.gateway.YnabMostRecentTransactionGateway
import com.pissiphany.bany.domain.repository.YnabLastKnowledgeOfServerRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalTime

internal class GetMostRecentUseCaseTest {
    @Test
    fun run__success() {
        val input = InputBoundary(Account("accountId", "name", 1L, false, Account.Type.CHECKING))
        val output = OutputBoundary()

        val transactions = listOf(
            Transaction("transactionId1", LocalTime.now(), 10L),
            Transaction("transactionId2", LocalTime.now(), 15L)
        )

        val cache = TestRepo()
        val gateway = TestGateway(transactions)

        val uc = GetMostRecentUseCase(cache, gateway)

        uc.run(input, output)

        assertEquals(transactions.first(), output.transaction)
    }

    private class InputBoundary(override val account: Account) : GetMostRecentInputBoundary
    private class OutputBoundary(override var transaction: Transaction? = null) : GetMostRecentOutputBoundary

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

    private class TestGateway(private val transactions: List<Transaction>) : YnabMostRecentTransactionGateway {
        override fun getUpdatedTransactions(lastKnowledgeOfServer: Int): UpdatedTransactions {
            if (lastKnowledgeOfServer > 0) {
                return UpdatedTransactions(transactions, lastKnowledgeOfServer + 1)
            }

            throw IllegalStateException("shouldn't be here!")
        }

    }
}