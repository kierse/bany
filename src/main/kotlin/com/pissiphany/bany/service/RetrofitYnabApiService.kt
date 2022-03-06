package com.pissiphany.bany.service

import com.pissiphany.bany.adapter.dataStructure.*
import com.pissiphany.bany.adapter.service.YnabApiService
import com.pissiphany.bany.dataStructure.RetrofitTransactions
import com.pissiphany.bany.mapper.RetrofitAccountMapper
import com.pissiphany.bany.mapper.RetrofitTransactionMapper
import com.pissiphany.bany.shared.logger
import okio.IOException

class RetrofitYnabApiService(
    private val service: RetrofitYnabService,
    private val accountMapper: RetrofitAccountMapper,
    private val transactionMapper: RetrofitTransactionMapper
) : YnabApiService {
    private val logger by logger()

    override suspend fun getAccount(budgetAccountIds: YnabBudgetAccountIds): YnabAccount? {
        val response = try {
            service.getAccount(budgetAccountIds.ynabBudgetId, budgetAccountIds.ynabAccountId)
        } catch (e: IOException) {
            logger.warn("Unable to get account: ${e.message}")
            return null
        }

        val account = response.process()
        if (account == null) {
            logger.warn("Unable to build RetrofitAccount")
            return null
        }

        return accountMapper.toYnabAccount(account)
    }

    override suspend fun getTransactions(
        budgetAccountIds: YnabBudgetAccountIds, serverKnowledge: Int?
    ): YnabUpdatedTransactions {
        val response = try {
            service.getTransactions(budgetAccountIds.ynabBudgetId, budgetAccountIds.ynabAccountId, serverKnowledge)
        } catch (e: IOException) {
            logger.error("Unable to get ynab transactions: ${e.message}")
            throw e
        }

        val transactionsWrapper = checkNotNull(response.process()) {
            "Unable to process response and obtain transaction wrapper!"
        }

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

        val response = try {
            service.saveTransactions(budgetAccountIds.ynabBudgetId, RetrofitTransactions(transactions))
        } catch (e: IOException) {
            logger.warn("Unable to save transactions: ${e.message}")
            return false
        }

        if (!response.isSuccessful) {
            logger.warn("Request unsuccessful: (HTTP ${response.code()}) ${response.message()}")
            return false
        }

        return true
    }
}