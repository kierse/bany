package com.pissiphany.bany.domain.gateway

import com.pissiphany.bany.domain.dataStructure.Budget

interface YnabBudgetGateway {
    fun getBudgets(): List<Budget>
}