package com.pissiphany.bany.domain.useCase.linkedAccounts

import com.pissiphany.bany.domain.dataStructure.BudgetAccount

interface GetBudgetAccountsOutputBoundary {
    var budgetAccounts: List<BudgetAccount>
}