package com.pissiphany.bany.domain.repository

import com.pissiphany.bany.domain.dataStructure.BudgetAccountIds

interface ConfigurationRepository {
    suspend fun getBudgetAccountIds(): List<BudgetAccountIds>
}