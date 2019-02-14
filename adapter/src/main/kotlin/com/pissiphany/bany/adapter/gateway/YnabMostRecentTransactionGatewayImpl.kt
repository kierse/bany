package com.pissiphany.bany.adapter.gateway

import com.pissiphany.bany.adapter.service.YnabService
import com.pissiphany.bany.domain.dataStructure.Account
import com.pissiphany.bany.domain.dataStructure.Budget
import com.pissiphany.bany.domain.dataStructure.Transaction
import com.pissiphany.bany.domain.gateway.YnabMostRecentTransactionGateway
import java.time.LocalDate

class YnabMostRecentTransactionGatewayImpl(private val ynabService: YnabService) : YnabMostRecentTransactionGateway {
    override fun getTransactionsSince(budget: Budget, account: Account, since: LocalDate): List<Transaction> {
        val call = ynabService.getTransactionsSince(budget.id, account.id, since)
        val response = call.execute()

        // TODO("error handling")
        return response.body().orEmpty()
    }

    override fun getTransactions(budget: Budget, account: Account): List<Transaction> {
        val call = ynabService.getTransaction(budget.id, account.id)
        val response = call.execute()

        return response.body().orEmpty()
    }
}