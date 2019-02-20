package com.pissiphany.bany.adapter.gateway

import com.pissiphany.bany.adapter.mapper.TransactionMapper
import com.pissiphany.bany.adapter.service.YnabService
import com.pissiphany.bany.domain.dataStructure.Account
import com.pissiphany.bany.domain.dataStructure.Budget
import com.pissiphany.bany.domain.dataStructure.UpdatedTransactions
import com.pissiphany.bany.domain.gateway.YnabMostRecentTransactionsGateway

class YnabMostRecentTransactionsGatewayImpl(
    private val ynabService: YnabService, private val mapper: TransactionMapper
) : YnabMostRecentTransactionsGateway {
    override fun getUpdatedTransactions(
        budget: Budget, account: Account, lastKnowledgeOfServer: Int
    ): UpdatedTransactions? {
        val call = ynabService.getTransactions(budget.id, account.id, lastKnowledgeOfServer)
        val response = call.execute()
        val ynabTransactionsWrapper = response.body() ?: return null

        val transactions = ynabTransactionsWrapper.ynabTransactions.map { mapper.toTransaction(it) }
        return UpdatedTransactions(transactions, ynabTransactionsWrapper.serverKnowledge)
    }
}