package com.pissiphany.bany.domain.gateway

import com.pissiphany.bany.domain.dataStructure.UpdatedTransactions

interface YnabMostRecentTransactionGateway {
    fun getUpdatedTransactions(lastKnowledgeOfServer: Int): UpdatedTransactions
}