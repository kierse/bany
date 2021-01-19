package com.pissiphany.bany.adapter.gateway

import com.pissiphany.bany.adapter.mapper.YnabBudgetAccountIdsMapper
import com.pissiphany.bany.adapter.mapper.YnabTransactionMapper
import com.pissiphany.bany.adapter.service.YnabApiService
import com.pissiphany.bany.domain.dataStructure.AccountTransaction
import com.pissiphany.bany.domain.dataStructure.BudgetAccountIds
import com.pissiphany.bany.domain.gateway.YnabSaveTransactionsGateway

class YnabSaveTransactionsGatewayImpl(
    private val service: YnabApiService,
    private val budgetAccountIdsMapper: YnabBudgetAccountIdsMapper,
    private val transactionMapper: YnabTransactionMapper
) : YnabSaveTransactionsGateway {
    override fun saveTransactions(budgetAccountIds: BudgetAccountIds, transactions: List<AccountTransaction>): Boolean {
        val ids = budgetAccountIdsMapper.toYnabBudgetAccountIds(budgetAccountIds)

        return transactions
            .map { transaction -> transactionMapper.toYnabAccountTransaction(ids, transaction) }
            .let { service.saveTransactions(ids, it) }
    }
}