package com.pissiphany.bany.adapter.gateway

import com.pissiphany.bany.adapter.service.YnabService
import com.pissiphany.bany.domain.dataStructure.UpdatedTransactions
import com.pissiphany.bany.domain.gateway.YnabMostRecentTransactionGateway

class YnabMostRecentTransactionGatewayImpl(private val ynabService: YnabService) : YnabMostRecentTransactionGateway {
    override fun getUpdatedTransactions(lastKnowledgeOfServer: Int): UpdatedTransactions {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}