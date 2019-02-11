package com.pissiphany.bany.domain.useCase

import com.pissiphany.bany.domain.dataStructure.Account
import com.pissiphany.bany.domain.dataStructure.Budget
import com.pissiphany.bany.domain.gateway.YnabBudgetGateway

class GetBudgetAccountsUseCase(private val ynabGateway: YnabBudgetGateway) {
    fun getAccounts(budget: Budget): List<Account> {
        TODO()
    }
}