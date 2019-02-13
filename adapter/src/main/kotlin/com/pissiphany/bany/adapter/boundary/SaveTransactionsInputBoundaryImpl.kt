package com.pissiphany.bany.adapter.boundary

import com.pissiphany.bany.domain.dataStructure.Account
import com.pissiphany.bany.domain.dataStructure.Budget
import com.pissiphany.bany.domain.dataStructure.Transaction
import com.pissiphany.bany.domain.useCase.ynabTransactions.SaveTransactionsInputBoundary

class SaveTransactionsInputBoundaryImpl(
    override val budget: Budget,
    override val account: Account,
    override val transactions: List<Transaction>
) : SaveTransactionsInputBoundary