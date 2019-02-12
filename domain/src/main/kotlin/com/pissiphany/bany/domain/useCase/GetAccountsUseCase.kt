package com.pissiphany.bany.domain.useCase

import com.pissiphany.bany.domain.dataStructure.Account
import com.pissiphany.bany.domain.dataStructure.Budget
import com.pissiphany.bany.domain.gateway.YnabAccountGateway

class GetAccountsUseCase(private val ynabGateway: YnabAccountGateway) {
    fun getActiveAccounts(budget: Budget): List<Account> {
        return ynabGateway.getAccounts(budget)
            .filterNot { it.closed }
    }
}