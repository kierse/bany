package com.pissiphany.bany.domain.useCase.ynabTransactions

import com.pissiphany.bany.domain.dataStructure.Account
import com.pissiphany.bany.domain.dataStructure.Budget

interface GetMostRecentInputBoundary {
    val budget: Budget
    val account: Account
}