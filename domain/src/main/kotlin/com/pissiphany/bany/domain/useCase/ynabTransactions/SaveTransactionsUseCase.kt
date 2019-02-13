package com.pissiphany.bany.domain.useCase.ynabTransactions

import com.pissiphany.bany.domain.dataStructure.Account
import com.pissiphany.bany.domain.dataStructure.Budget
import com.pissiphany.bany.domain.dataStructure.Transaction
import com.pissiphany.bany.domain.gateway.SaveTransactionsToYnabGateway

class SaveTransactionsUseCase(private val ynabGateway: SaveTransactionsToYnabGateway) {
    fun run(input: SaveTransactionsInputBoundary) {
        ynabGateway.saveTransactions(input.budget, input.account, input.transactions)
    }
}