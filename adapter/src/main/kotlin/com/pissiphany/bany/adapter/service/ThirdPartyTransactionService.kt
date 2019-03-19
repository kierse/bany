package com.pissiphany.bany.adapter.service

import com.pissiphany.bany.adapter.dataStructure.YnabTransaction
import com.pissiphany.bany.plugin.dataStructure.YnabBudgetAccountIds
import java.time.LocalDate

interface ThirdPartyTransactionService {
    fun getYnabBudgetAccountIds(): List<YnabBudgetAccountIds>
    fun getNewYnabTransactionsSince(budgetAccountIds: YnabBudgetAccountIds, date: LocalDate?): List<YnabTransaction>
}