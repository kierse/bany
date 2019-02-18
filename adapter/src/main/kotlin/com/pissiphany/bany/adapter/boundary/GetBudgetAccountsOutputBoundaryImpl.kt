package com.pissiphany.bany.adapter.boundary

import com.pissiphany.bany.domain.dataStructure.BudgetAccount
import com.pissiphany.bany.domain.useCase.budgetAccounts.GetBudgetAccountsOutputBoundary

class GetBudgetAccountsOutputBoundaryImpl : GetBudgetAccountsOutputBoundary {
    override var budgetAccounts: List<BudgetAccount> = emptyList()
}