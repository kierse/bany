package com.pissiphany.bany.domain.gateway

import com.pissiphany.bany.domain.dataStructure.BudgetAccountIds

interface ConfigurationRepository {
    fun getBudgetAccountIds(): List<BudgetAccountIds>
}