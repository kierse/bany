package com.pissiphany.bany.domain.useCase.step

import com.pissiphany.bany.domain.dataStructure.Budget
import com.pissiphany.bany.domain.dataStructure.BudgetAccount
import com.pissiphany.bany.domain.repository.ConfigurationRepository
import com.pissiphany.bany.domain.gateway.YnabBudgetAccountsGateway
import com.pissiphany.bany.domain.useCase.SyncThirdPartyTransactionsUseCase
import java.lang.IllegalArgumentException

class GetBudgetAccounts(
    private val repo: ConfigurationRepository, private val service: YnabBudgetAccountsGateway
) : SyncThirdPartyTransactionsUseCase.Step1GetBudgetAccounts {
    override fun getBudgetAccounts(): List<BudgetAccount> {
        val budgets = mutableMapOf<String, Budget>()
        val budgetAccounts = mutableListOf<BudgetAccount>()

        for ((budgetId, accountId) in repo.getBudgetAccountIds()) {
            val budget = getBudget(budgetId, budgets)
            val account = service.getAccount(budgetId, accountId) ?: throw IllegalArgumentException("unable to find account $accountId")

            budgetAccounts.add(BudgetAccount(budget, account))
        }

        return budgetAccounts
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