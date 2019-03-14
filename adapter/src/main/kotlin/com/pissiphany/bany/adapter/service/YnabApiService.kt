package com.pissiphany.bany.adapter.service

import com.pissiphany.bany.adapter.dataStructure.YnabAccount
import com.pissiphany.bany.adapter.dataStructure.YnabBudget
import com.pissiphany.bany.adapter.dataStructure.YnabTransaction
import com.pissiphany.bany.adapter.dataStructure.YnabUpdatedTransactions

interface YnabApiService {
    fun getBudget(budgetId: String): YnabBudget?

    fun getAccount(budgetId: String, accountId: String): YnabAccount?

    fun getTransactions(budget: YnabBudget, account: YnabAccount, serverKnowledge: Int? = null): YnabUpdatedTransactions

    fun saveTransactions(budget: YnabBudget, ynabTransactions: List<YnabTransaction>): Boolean
}
