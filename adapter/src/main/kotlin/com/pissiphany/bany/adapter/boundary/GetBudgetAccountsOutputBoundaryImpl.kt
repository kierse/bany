package com.pissiphany.bany.adapter.boundary

import com.pissiphany.bany.domain.dataStructure.BudgetAccount
import com.pissiphany.bany.domain.useCase.linkedAccounts.GetBudgetAccountsOutputBoundary

class GetBudgetAccountsOutputBoundaryImpl : GetBudgetAccountsOutputBoundary {
    override var budgetAccounts: List<BudgetAccount> = emptyList()
}