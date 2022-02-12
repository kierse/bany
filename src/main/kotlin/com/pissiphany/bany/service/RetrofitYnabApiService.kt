package com.pissiphany.bany.service

import com.pissiphany.bany.adapter.dataStructure.*
import com.pissiphany.bany.adapter.service.YnabApiService
import com.pissiphany.bany.dataStructure.RetrofitTransactions
import com.pissiphany.bany.mapper.RetrofitAccountMapper
import com.pissiphany.bany.mapper.RetrofitTransactionMapper

class RetrofitYnabApiService(
    private val service: RetrofitYnabService,
    private val accountMapper: RetrofitAccountMapper,
    private val transactionMapper: RetrofitTransactionMapper
) : YnabApiService {
    override suspend fun getAccount(budgetAccountIds: YnabBudgetAccountIds): YnabAccount? {
        val call = service.getAccount(budgetAccountIds.ynabBudgetId, budgetAccountIds.ynabAccountId)
        val response = call.execute()
        val account = response.body() ?: return null

        return accountMapper.toYnabAccount(account)
    }

    override suspend fun getTransactions(
        budgetAccountIds: YnabBudgetAccountIds, serverKnowledge: Int?
    ): YnabUpdatedTransactions {
        val call = service.getTransactions(budgetAccountIds.ynabBudgetId, budgetAccountIds.ynabAccountId, serverKnowledge)
        val response = call.execute()
        val transactionsWrapper = response.body() ?: TODO("throw error!")

        val transactions = transactionsWrapper
            .transactions
            .map { transactionMapper.toYnabTransaction(budgetAccountIds, it) }

        return YnabUpdatedTransactions(transactions, transactionsWrapper.server_knowledge)
    }

    override suspend fun saveTransactions(
        budgetAccountIds: YnabBudgetAccountIds, ynabTransactions: List<YnabAccountTransaction>
    ): Boolean {
        val transactions = ynabTransactions.map { transaction ->
            transactionMapper.toRetrofitTransaction(transaction)
        }

        val call = service.saveTransactions(budgetAccountIds.ynabBudgetId, RetrofitTransactions(transactions))
        return call.execute().isSuccessful
    }
}