package com.pissiphany.bany.domain.useCase.linkedAccounts

import com.pissiphany.bany.domain.dataStructure.BudgetAccount

interface GetLinkedAccountsOutputBoundary {
    var linkedAccounts: List<BudgetAccount>
}