package com.pissiphany.bany.domain.useCase.ynabTransactions

import com.pissiphany.bany.domain.dataStructure.Account
import com.pissiphany.bany.domain.dataStructure.Budget
import com.pissiphany.bany.domain.dataStructure.Transaction

interface SaveTransactionsInputBoundary {
    val budget: Budget
    val account: Account
    val transactions: List<Transaction>
}