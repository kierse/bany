package com.pissiphany.bany.adapter.gateway

import com.pissiphany.bany.adapter.mapper.YnabAccountMapper
import com.pissiphany.bany.adapter.mapper.YnabBudgetMapper
import com.pissiphany.bany.adapter.mapper.YnabTransactionMapper
import com.pissiphany.bany.adapter.service.YnabApiService
import com.pissiphany.bany.domain.dataStructure.Account
import com.pissiphany.bany.domain.dataStructure.Budget
import com.pissiphany.bany.domain.dataStructure.UpdatedTransactions
import com.pissiphany.bany.domain.gateway.YnabMostRecentTransactionsGateway

class YnabMostRecentTransactionsGatewayImpl(
    private val service: YnabApiService,
    private val budgetMapper: YnabBudgetMapper,
    private val accountMapper: YnabAccountMapper,
    private val transactionMapper: YnabTransactionMapper
) : YnabMostRecentTransactionsGateway {
    override fun getUpdatedTransactions(
        budget: Budget, account: Account, lastKnowledgeOfServer: Int
    ): UpdatedTransactions {
        val (transactions, newServerKnowledge) = service.getTransactions(
            budgetMapper.toYnabBudget(budget), accountMapper.toYnabAccount(account), lastKnowledgeOfServer
        )

        val ynabTransactions = transactions.map { transactionMapper.toTransaction(it) }
        return UpdatedTransactions(ynabTransactions, newServerKnowledge)
    }
}