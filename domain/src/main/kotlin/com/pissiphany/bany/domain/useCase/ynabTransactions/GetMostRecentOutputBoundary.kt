package com.pissiphany.bany.domain.useCase.ynabTransactions

import com.pissiphany.bany.domain.dataStructure.Transaction

interface GetMostRecentOutputBoundary {
    var transaction: Transaction?
}