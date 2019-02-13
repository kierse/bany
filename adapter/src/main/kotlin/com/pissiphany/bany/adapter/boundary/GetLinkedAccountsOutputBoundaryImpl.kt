package com.pissiphany.bany.adapter.boundary

import com.pissiphany.bany.domain.dataStructure.BudgetAccount
import com.pissiphany.bany.domain.useCase.linkedAccounts.GetLinkedAccountsOutputBoundary

class GetLinkedAccountsOutputBoundaryImpl : GetLinkedAccountsOutputBoundary {
    override var linkedAccounts: List<BudgetAccount> = emptyList()
}