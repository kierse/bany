package com.pissiphany.bany.domain.useCase

import com.pissiphany.bany.domain.dataStructure.Budget
import com.pissiphany.bany.domain.gateway.YnabBudgetGateway

class GetBudgetsUseCase(private val ynabGateway: YnabBudgetGateway) {
    fun getBudgets(): List<Budget> {
        TODO()
    }
}