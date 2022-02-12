package com.pissiphany.bany.adapter.service

import com.pissiphany.bany.adapter.dataStructure.*

interface YnabApiService {
    suspend fun getAccount(budgetAccountIds: YnabBudgetAccountIds): YnabAccount?

    suspend fun getTransactions(budgetAccountIds: YnabBudgetAccountIds, serverKnowledge: Int? = null): YnabUpdatedTransactions

    suspend fun saveTransactions(budgetAccountIds: YnabBudgetAccountIds, ynabTransactions: List<YnabAccountTransaction>): Boolean
}
