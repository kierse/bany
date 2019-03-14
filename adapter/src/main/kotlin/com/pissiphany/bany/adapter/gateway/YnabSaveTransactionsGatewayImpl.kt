package com.pissiphany.bany.adapter.gateway

import com.pissiphany.bany.adapter.mapper.YnabBudgetMapper
import com.pissiphany.bany.adapter.mapper.YnabTransactionMapper
import com.pissiphany.bany.adapter.service.YnabApiService
import com.pissiphany.bany.domain.dataStructure.Account
import com.pissiphany.bany.domain.dataStructure.Budget
import com.pissiphany.bany.domain.dataStructure.Transaction
import com.pissiphany.bany.domain.gateway.YnabSaveTransactionsGateway

class YnabSaveTransactionsGatewayImpl(
    private val service: YnabApiService,
    private val budgetMapper: YnabBudgetMapper,
    private val transactionMapper: YnabTransactionMapper
) : YnabSaveTransactionsGateway {
    override fun saveTransactions(budget: Budget, account: Account, domainTransactions: List<Transaction>): Boolean {
        val transactions = domainTransactions.map { transaction ->
            transactionMapper.toYnabTransaction(transaction, account)
        }

        return service.saveTransactions(budgetMapper.toYnabBudget(budget), transactions)
    }
}