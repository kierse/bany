package com.pissiphany.bany.domain.useCase.ynabTransactions

import com.pissiphany.bany.domain.gateway.YnabMostRecentTransactionGateway
import java.time.LocalDate

class GetMostRecentUseCase(private val ynabGateway: YnabMostRecentTransactionGateway) {
    fun run(input: GetMostRecentInputBoundary, output: GetMostRecentOutputBoundary) {
        val threeDaysAgo = LocalDate.now().minusDays(3)
        var transactions = ynabGateway.getTransactionsSince(input.budget, input.account, threeDaysAgo)
        if (transactions.isNotEmpty()) {
            output.transaction = transactions.first()
            return
        }

        val sevenDaysAgo = threeDaysAgo.minusDays(4)
        transactions = ynabGateway.getTransactionsSince(input.budget, input.account, sevenDaysAgo)
        if (transactions.isNotEmpty()) {
            output.transaction = transactions.first()
            return
        }

        // finally, try and get whatever we can
        output.transaction = ynabGateway.getTransactions(input.budget, input.account).firstOrNull()
    }
}