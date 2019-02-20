package com.pissiphany.bany.domain.useCase.budgetAccounts

import com.pissiphany.bany.domain.dataStructure.Budget
import com.pissiphany.bany.domain.dataStructure.BudgetAccount
import com.pissiphany.bany.domain.repository.ConfigurationRepository
import com.pissiphany.bany.domain.gateway.YnabBudgetAccountsGateway
import java.lang.IllegalArgumentException

class GetBudgetAccountsUseCase(private val repo: ConfigurationRepository, private val service: YnabBudgetAccountsGateway) {
    fun run(output: GetBudgetAccountsOutputBoundary) {
        val budgets = mutableMapOf<String, Budget>()
        val budgetAccounts = mutableListOf<BudgetAccount>()

        for ((budgetId, accountId) in repo.getBudgetAccountIds()) {
            val budget = getBudget(budgetId, budgets)
            val account = service.getAccount(budgetId, accountId) ?: throw IllegalArgumentException("unable to find account $accountId")

            budgetAccounts.add(BudgetAccount(budget, account))
        }

        output.budgetAccounts = budgetAccounts
    }

    private fun getBudget(budgetId: String, cache: MutableMap<String, Budget>): Budget {
        var budget = cache[budgetId]
        if (budget == null) {
            budget = service.getBudget(budgetId) ?:
                    throw IllegalArgumentException("unable to find budget $budgetId")
            cache[budgetId] = budget
        }

        return budget
    }
}