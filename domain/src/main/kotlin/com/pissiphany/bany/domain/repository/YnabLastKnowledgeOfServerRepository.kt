package com.pissiphany.bany.domain.repository

import com.pissiphany.bany.domain.dataStructure.BudgetAccountIds

interface YnabLastKnowledgeOfServerRepository {
    fun getLastKnowledgeOfServer(budgetAccountIds: BudgetAccountIds): Int
    fun saveLastKnowledgeOfServer(budgetAccountIds: BudgetAccountIds, lastKnowledgeOfServer: Int)
}