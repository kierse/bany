package com.pissiphany.bany.domain.useCase.ynabTransactions

import com.pissiphany.bany.domain.gateway.YnabSaveTransactionsGateway

class SaveTransactionsUseCase(private val ynabGateway: YnabSaveTransactionsGateway) {
    fun run(input: SaveTransactionsInputBoundary) {
        ynabGateway.saveTransactions(input.budget, input.account, input.transactions)
    }
}