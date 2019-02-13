package com.pissiphany.bany.domain.useCase

import com.pissiphany.bany.domain.dataStructure.Account
import com.pissiphany.bany.domain.dataStructure.Budget
import com.pissiphany.bany.domain.dataStructure.Transaction
import com.pissiphany.bany.domain.gateway.SaveTransactionsToYnabGateway

class SaveTransactionsToYnabUseCase(private val ynabGateway: SaveTransactionsToYnabGateway) {
    fun saveTransactions(budget: Budget, account: Account, transactions: List<Transaction>) =
            ynabGateway.saveTransactions(budget, account, transactions)
}