package com.pissiphany.bany.domain.gateway

import com.pissiphany.bany.domain.dataStructure.Account
import com.pissiphany.bany.domain.dataStructure.Budget
import com.pissiphany.bany.domain.dataStructure.UpdatedTransactions

interface YnabMostRecentTransactionGateway {
    fun getUpdatedTransactions(budget: Budget, account: Account, lastKnowledgeOfServer: Int): UpdatedTransactions?
}