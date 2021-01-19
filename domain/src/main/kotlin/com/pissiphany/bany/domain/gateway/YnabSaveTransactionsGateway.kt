package com.pissiphany.bany.domain.gateway

import com.pissiphany.bany.domain.dataStructure.AccountTransaction
import com.pissiphany.bany.domain.dataStructure.BudgetAccountIds

interface YnabSaveTransactionsGateway {
    fun saveTransactions(budgetAccountIds: BudgetAccountIds, transactions: List<AccountTransaction>): Boolean
}