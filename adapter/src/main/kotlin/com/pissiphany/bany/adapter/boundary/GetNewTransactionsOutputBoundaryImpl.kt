package com.pissiphany.bany.adapter.boundary

import com.pissiphany.bany.domain.dataStructure.Transaction
import com.pissiphany.bany.domain.useCase.thirdPartyTransactions.GetNewTransactionsOutputBoundary

class GetNewTransactionsOutputBoundaryImpl : GetNewTransactionsOutputBoundary {
    override var transactions: List<Transaction> = emptyList()
}