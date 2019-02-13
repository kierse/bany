package com.pissiphany.bany.adapter.gateway

import com.pissiphany.bany.adapter.service.YnabService
import com.pissiphany.bany.domain.dataStructure.Account
import com.pissiphany.bany.domain.dataStructure.Budget
import com.pissiphany.bany.domain.dataStructure.Transaction
import com.pissiphany.bany.domain.gateway.YnabMostRecentTransactionGateway
import java.time.LocalDate

class YnabMostRecentTransactionGatewayImpl(private val ynabService: YnabService) : YnabMostRecentTransactionGateway {
    override fun getTransactionsSince(budget: Budget, account: Account, since: LocalDate): List<Transaction> {
        return ynabService.getTransactionsSince(budget, account, since)
    }

    override fun getTransactions(budget: Budget, account: Account): List<Transaction> {
        return ynabService.getTransaction(budget, account)
    }
}