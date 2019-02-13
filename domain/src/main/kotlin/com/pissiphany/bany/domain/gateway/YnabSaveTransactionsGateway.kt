package com.pissiphany.bany.domain.gateway

import com.pissiphany.bany.domain.dataStructure.Account
import com.pissiphany.bany.domain.dataStructure.Budget
import com.pissiphany.bany.domain.dataStructure.Transaction

interface YnabSaveTransactionsGateway {
    fun saveTransactions(budget: Budget, account: Account, transactions: List<Transaction>)
}