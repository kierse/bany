package com.pissiphany.bany.domain.gateway

import com.pissiphany.bany.domain.dataStructure.Account
import com.pissiphany.bany.domain.dataStructure.BudgetAccountIds

interface YnabBudgetAccountsGateway {
    fun getAccount(budgetAccountIds: BudgetAccountIds): Account?
}