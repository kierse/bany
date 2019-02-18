package com.pissiphany.bany.domain.useCase.linkedAccounts

import com.pissiphany.bany.domain.dataStructure.Budget
import com.pissiphany.bany.domain.dataStructure.BudgetAccount
import com.pissiphany.bany.domain.gateway.ConfigurationRepository
import com.pissiphany.bany.domain.gateway.YnabBudgetAccountsGateway

class GetBudgetAccountsUseCase(private val repo: ConfigurationRepository, private val service: YnabBudgetAccountsGateway) {
    fun run(output: GetBudgetAccountsOutputBoundary) {
        val budgets = mutableMapOf<String, Budget>()
        val budgetAccounts = mutableListOf<BudgetAccount>()

        for ((budgetId, accountId) in repo.getBudgetAccountIds()) {
            val budget = getBudget(budgetId, budgets)
            val account = service.getAccount(accountId)

            budgetAccounts.add(BudgetAccount(budget, account))
        }

        output.budgetAccounts = budgetAccounts
    }

    private fun getBudget(budgetId: String, cache: MutableMap<String, Budget>): Budget {
        var budget = cache[budgetId]
        if (budget == null) {
            budget = service.getBudget(budgetId)
        }

        return budget
    }
}