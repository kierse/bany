package com.pissiphany.bany.domain.gateway

import com.pissiphany.bany.domain.dataStructure.Account
import com.pissiphany.bany.domain.dataStructure.Budget

interface ThirdPartyTransactionGatewayFactory {
    fun getGateway(budget: Budget, account: Account): ThirdPartyTransactionGateway
}