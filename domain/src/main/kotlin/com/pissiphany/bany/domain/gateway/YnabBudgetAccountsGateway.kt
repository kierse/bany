package com.pissiphany.bany.domain.gateway

import com.pissiphany.bany.domain.dataStructure.Account
import com.pissiphany.bany.domain.dataStructure.Budget

interface YnabBudgetAccountsGateway {
    fun getBudget(budgetId: String): Budget
    fun getAccount(accountId: String): Account
}