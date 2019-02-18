package com.pissiphany.bany.domain.repository

import com.pissiphany.bany.domain.dataStructure.BudgetAccountIds

interface ConfigurationRepository {
    fun getBudgetAccountIds(): List<BudgetAccountIds>
}