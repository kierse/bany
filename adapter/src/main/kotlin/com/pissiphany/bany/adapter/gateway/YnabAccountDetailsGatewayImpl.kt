package com.pissiphany.bany.adapter.gateway

import com.pissiphany.bany.adapter.mapper.YnabAccountMapper
import com.pissiphany.bany.adapter.mapper.YnabBudgetAccountIdsMapper
import com.pissiphany.bany.adapter.mapper.YnabTransactionMapper
import com.pissiphany.bany.adapter.service.YnabApiService
import com.pissiphany.bany.domain.dataStructure.Account
import com.pissiphany.bany.domain.dataStructure.BudgetAccountIds
import com.pissiphany.bany.domain.dataStructure.UpdatedTransactions
import com.pissiphany.bany.domain.gateway.YnabAccountDetailsGateway

class YnabAccountDetailsGatewayImpl(
    private val service: YnabApiService,
    private val budgetAccountIdsMapper: YnabBudgetAccountIdsMapper,
    private val accountMapper: YnabAccountMapper,
    private val transactionMapper: YnabTransactionMapper
) : YnabAccountDetailsGateway {
    override fun getAccount(budgetAccountIds: BudgetAccountIds): Account? {
        val ids = budgetAccountIdsMapper.toYnabBudgetAccountIds(budgetAccountIds)
        return service.getAccount(ids)
            ?.let { accountMapper.toAccount(it) }
    }

    override fun getUpdatedTransactions(
        budgetAccountIds: BudgetAccountIds, lastKnowledgeOfServer: Int
    ): UpdatedTransactions {
        val ids = budgetAccountIdsMapper.toYnabBudgetAccountIds(budgetAccountIds)
        val (transactions, newServerKnowledge) = service.getTransactions(ids, lastKnowledgeOfServer)

        val accountTransactions = transactions
            .map { transactionMapper.toAccountTransaction(it) }
        return UpdatedTransactions(accountTransactions, newServerKnowledge)
    }
}