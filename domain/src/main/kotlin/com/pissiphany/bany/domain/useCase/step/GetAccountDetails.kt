package com.pissiphany.bany.domain.useCase.step

import com.pissiphany.bany.domain.dataStructure.AccountAndTransaction
import com.pissiphany.bany.domain.dataStructure.BudgetAccountIds
import com.pissiphany.bany.domain.gateway.YnabAccountDetailsGateway
import com.pissiphany.bany.domain.repository.YnabLastKnowledgeOfServerRepository
import com.pissiphany.bany.domain.useCase.SyncThirdPartyTransactionsUseCase

class GetAccountDetails(
    private val ynabCache: YnabLastKnowledgeOfServerRepository,
    private val ynabGateway: YnabAccountDetailsGateway
) : SyncThirdPartyTransactionsUseCase.Step1GetAccountDetails {
    override suspend fun getAccountAndLastTransaction(budgetAccountIds: BudgetAccountIds): AccountAndTransaction {
        val account = checkNotNull(ynabGateway.getAccount(budgetAccountIds)) { "Unable to fetch account: $budgetAccountIds" }

        val cachedLastKnowledge = ynabCache.getLastKnowledgeOfServer(budgetAccountIds)
        val (transactions, newLastKnowledge) = ynabGateway.getUpdatedTransactions(
            budgetAccountIds, cachedLastKnowledge
        )

        ynabCache.saveLastKnowledgeOfServer(budgetAccountIds, newLastKnowledge)
        return AccountAndTransaction(account, transactions.firstOrNull())
    }
}