package com.pissiphany.bany.domain.repository

import com.pissiphany.bany.domain.dataStructure.BudgetAccountIds

interface YnabLastKnowledgeOfServerRepository {
    suspend fun getLastKnowledgeOfServer(budgetAccountIds: BudgetAccountIds): Int
    suspend fun saveLastKnowledgeOfServer(budgetAccountIds: BudgetAccountIds, lastKnowledgeOfServer: Int)
}