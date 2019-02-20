package com.pissiphany.bany.domain.useCase.ynabTransactions

import com.pissiphany.bany.domain.gateway.YnabMostRecentTransactionsGateway
import com.pissiphany.bany.domain.repository.YnabLastKnowledgeOfServerRepository
import java.lang.IllegalArgumentException

class GetMostRecentUseCase(
    private val ynabCache: YnabLastKnowledgeOfServerRepository,
    private val ynabGateway: YnabMostRecentTransactionsGateway
) {
    fun run(input: GetMostRecentInputBoundary, output: GetMostRecentOutputBoundary) {
        val cachedLastKnowledge = ynabCache.getLastKnowledgeOfServer(input.account)
        val (transactions, newLastKnowledge) =
            ynabGateway.getUpdatedTransactions(input.budget, input.account, cachedLastKnowledge) ?:
                throw IllegalArgumentException("unable to retrieve transactions for ${input.account.id}")

        ynabCache.saveLastKnowledgeOfServer(input.account, newLastKnowledge)

        // TODO may have to filter / ignore certain transactions here
        // TODO make sure #first is returning the correct transaction
        output.transaction = transactions.first()
    }
}