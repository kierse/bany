package com.pissiphany.bany.adapter.boundary

import com.pissiphany.bany.domain.dataStructure.Transaction
import com.pissiphany.bany.domain.useCase.ynabTransactions.GetMostRecentOutputBoundary

class GetMostRecentOutputBoundaryImpl : GetMostRecentOutputBoundary {
    override var transaction: Transaction? = null
}