package com.pissiphany.bany.domain.gateway

import com.pissiphany.bany.domain.dataStructure.BudgetAccountIds

interface ThirdPartyTransactionGatewayFactory {
    fun getGateway(budgetAccountIds: BudgetAccountIds): ThirdPartyTransactionGateway
}