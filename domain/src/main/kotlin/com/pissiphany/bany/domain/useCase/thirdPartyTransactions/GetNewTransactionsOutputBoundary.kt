package com.pissiphany.bany.domain.useCase.thirdPartyTransactions

import com.pissiphany.bany.domain.dataStructure.Transaction

interface GetNewTransactionsOutputBoundary {
    var transactions: List<Transaction>
}