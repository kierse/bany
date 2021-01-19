package com.pissiphany.bany.adapter.service

import com.pissiphany.bany.adapter.dataStructure.*

interface YnabApiService {
    fun getAccount(budgetAccountIds: YnabBudgetAccountIds): YnabAccount?

    fun getTransactions(budgetAccountIds: YnabBudgetAccountIds, serverKnowledge: Int? = null): YnabUpdatedTransactions

    fun saveTransactions(budgetAccountIds: YnabBudgetAccountIds, ynabTransactions: List<YnabAccountTransaction>): Boolean
}
