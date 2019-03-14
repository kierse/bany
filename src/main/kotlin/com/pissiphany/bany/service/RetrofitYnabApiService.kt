package com.pissiphany.bany.service

import com.pissiphany.bany.adapter.dataStructure.YnabAccount
import com.pissiphany.bany.adapter.dataStructure.YnabBudget
import com.pissiphany.bany.adapter.dataStructure.YnabTransaction
import com.pissiphany.bany.adapter.dataStructure.YnabUpdatedTransactions
import com.pissiphany.bany.adapter.service.YnabApiService
import com.pissiphany.bany.dataStructure.RetrofitTransactions
import com.pissiphany.bany.mapper.RetrofitAccountMapper
import com.pissiphany.bany.mapper.RetrofitBudgetMapper
import com.pissiphany.bany.mapper.RetrofitTransactionMapper

class RetrofitYnabApiService(
    private val service: RetrofitYnabService,
    private val budgetMapper: RetrofitBudgetMapper,
    private val accountMapper: RetrofitAccountMapper,
    private val transactionMapper: RetrofitTransactionMapper
    ) : YnabApiService {
    override fun getBudget(budgetId: String): YnabBudget? {
        val call = service.getBudget(budgetId)
        val response = call.execute()
        val budgetWrapper = response.body() ?: return null

        return budgetMapper.toYnabBudget(budgetWrapper.budget)
    }

    override fun getAccount(budgetId: String, accountId: String): YnabAccount? {
        val call = service.getAccount(budgetId, accountId)
        val response = call.execute()
        val account = response.body() ?: return null

        return accountMapper.toYnabAccount(account)
    }

    override fun getTransactions(
        budget: YnabBudget, account: YnabAccount, serverKnowledge: Int?
    ): YnabUpdatedTransactions {
        val call = service.getTransactions(budget.id, account.id, serverKnowledge)
        val response = call.execute()
        val transactionsWrapper = response.body() ?: TODO("throw error!")

        val transactions = transactionsWrapper
            .transactions
            .map { transactionMapper.toYnabTransaction(it, account) }

        return YnabUpdatedTransactions(transactions, transactionsWrapper.server_knowledge)
    }

    override fun saveTransactions(
        budget: YnabBudget, ynabTransactions: List<YnabTransaction>
    ): Boolean {
        val transactions = ynabTransactions.map { transaction ->
            transactionMapper.toRetrofitTransaction(transaction)
        }

        val call = service.saveTransactions(budget.id, RetrofitTransactions(transactions))
        return call.execute().isSuccessful
    }
}