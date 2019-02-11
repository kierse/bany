package com.pissiphany.bany.domain.gateway

import com.pissiphany.bany.domain.dataStructure.Account
import com.pissiphany.bany.domain.dataStructure.Budget

interface YnabAccountGateway {
    fun getBudgetAccount(budget: Budget): List<Account>
}