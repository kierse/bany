package com.pissiphany.bany.adapter.boundary

import com.pissiphany.bany.domain.dataStructure.Account
import com.pissiphany.bany.domain.dataStructure.Budget
import com.pissiphany.bany.domain.useCase.ynabTransactions.GetMostRecentInputBoundary

class GetMostRecentInputBoundaryImpl(
    override val budget: Budget, override val account: Account
) : GetMostRecentInputBoundary