package com.pissiphany.bany.domain.gateway

import com.pissiphany.bany.domain.dataStructure.Account
import com.pissiphany.bany.domain.dataStructure.Budget
import com.pissiphany.bany.domain.dataStructure.Transaction
import java.time.LocalDate

interface YnabMostRecentTransactionGateway {
    fun getTransactionsSince(budget: Budget, account: Account, since: LocalDate): List<Transaction>
    fun getTransactions(budget: Budget, account: Account): List<Transaction>
}