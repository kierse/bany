package com.pissiphany.bany.domain.useCase.budgetAccounts

import com.pissiphany.bany.domain.dataStructure.BudgetAccount

interface GetBudgetAccountsOutputBoundary {
    var budgetAccounts: List<BudgetAccount>
}