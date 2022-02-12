package com.pissiphany.bany.adapter.service

import com.pissiphany.bany.adapter.dataStructure.YnabTransaction
import com.pissiphany.bany.adapter.dataStructure.YnabBudgetAccountIds
import java.time.LocalDate

interface ThirdPartyTransactionService {
    fun getYnabBudgetAccountIds(): List<YnabBudgetAccountIds>
    suspend fun getNewYnabTransactionsSince(budgetAccountIds: YnabBudgetAccountIds, date: LocalDate?): List<YnabTransaction>
}