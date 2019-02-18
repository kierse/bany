package com.pissiphany.bany.domain.useCase.ynabTransactions

import com.pissiphany.bany.domain.gateway.YnabMostRecentTransactionGateway
import com.pissiphany.bany.domain.repository.YnabLastKnowledgeOfServerRepository

class GetMostRecentUseCase(
    private val ynabCache: YnabLastKnowledgeOfServerRepository,
    private val ynabGateway: YnabMostRecentTransactionGateway
) {
    fun run(input: GetMostRecentInputBoundary, output: GetMostRecentOutputBoundary) {
        val cachedLastKnowledge = ynabCache.getLastKnowledgeOfServer(input.account)
        val (transactions, newLastKnowledge) = ynabGateway.getUpdatedTransactions(cachedLastKnowledge)

        ynabCache.saveLastKnowledgeOfServer(newLastKnowledge)

        // TODO may have to filter / ignore certain transactions here
        // TODO make sure #first is returning the correct transaction
        output.transaction = transactions.first()
    }
}