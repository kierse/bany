package com.pissiphany.bany.adapter.gateway

import com.pissiphany.bany.adapter.dataStructure.YnabTransactions
import com.pissiphany.bany.adapter.mapper.TransactionMapper
import com.pissiphany.bany.adapter.service.YnabService
import com.pissiphany.bany.domain.dataStructure.Account
import com.pissiphany.bany.domain.dataStructure.Budget
import com.pissiphany.bany.domain.dataStructure.Transaction
import com.pissiphany.bany.domain.gateway.YnabSaveTransactionsGateway

class YnabSaveTransactionsGatewayImpl(
    private val ynabService: YnabService, private val mapper: TransactionMapper
) : YnabSaveTransactionsGateway {
    override fun saveTransactions(budget: Budget, account: Account, domainTransactions: List<Transaction>): Boolean {
        val transactions = domainTransactions.map { transaction ->
            mapper.toYnabTransaction(transaction, account)
        }

        val call = ynabService.saveTransactions(budget.id, YnabTransactions(transactions))
        return call.execute().isSuccessful
    }
}