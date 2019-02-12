package com.pissiphany.bany.domain.useCase

import com.pissiphany.bany.domain.dataStructure.Account
import com.pissiphany.bany.domain.dataStructure.Budget
import com.pissiphany.bany.domain.dataStructure.Transaction
import com.pissiphany.bany.domain.gateway.YnabTransactionGateway
import java.time.LocalDate

class GetMostRecentTransactionUseCase(private val ynabGateway: YnabTransactionGateway) {
    fun getTransaction(budget: Budget, account: Account): Transaction? {
        val threeDaysAgo = LocalDate.now().minusDays(3)
        var transactions = ynabGateway.getTransactionsSince(budget, account, threeDaysAgo)
        if (transactions.isNotEmpty()) return transactions.first()

        val sevenDaysAgo = threeDaysAgo.minusDays(4)
        transactions = ynabGateway.getTransactionsSince(budget, account, sevenDaysAgo)
        if (transactions.isNotEmpty()) return transactions.first()

        return ynabGateway.getTransactions(budget, account).first()
    }
}