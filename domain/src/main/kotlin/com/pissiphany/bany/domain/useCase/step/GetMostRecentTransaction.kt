package com.pissiphany.bany.domain.useCase.step

import com.pissiphany.bany.domain.dataStructure.Account
import com.pissiphany.bany.domain.dataStructure.Budget
import com.pissiphany.bany.domain.dataStructure.Transaction
import com.pissiphany.bany.domain.gateway.YnabMostRecentTransactionsGateway
import com.pissiphany.bany.domain.repository.YnabLastKnowledgeOfServerRepository
import com.pissiphany.bany.domain.useCase.SyncThirdPartyTransactionsUseCase
import java.lang.IllegalArgumentException

class GetMostRecentTransaction(
    private val ynabCache: YnabLastKnowledgeOfServerRepository,
    private val ynabGateway: YnabMostRecentTransactionsGateway
) : SyncThirdPartyTransactionsUseCase.Step2GetMostRecentTransaction {
    override fun getTransaction(budget: Budget, account: Account): Transaction? {
        val cachedLastKnowledge = ynabCache.getLastKnowledgeOfServer(account)
        val (transactions, newLastKnowledge) = ynabGateway.getUpdatedTransactions(budget, account, cachedLastKnowledge)

        ynabCache.saveLastKnowledgeOfServer(account, newLastKnowledge)
        return transactions.firstOrNull()
    }
}