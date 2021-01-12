package com.pissiphany.bany.adapter.factory

import com.pissiphany.bany.adapter.gateway.ThirdPartyTransactionGatewayImpl
import com.pissiphany.bany.adapter.mapper.YnabTransactionMapper
import com.pissiphany.bany.adapter.service.ThirdPartyTransactionService
import com.pissiphany.bany.domain.dataStructure.Account
import com.pissiphany.bany.domain.dataStructure.Budget
import com.pissiphany.bany.domain.gateway.ThirdPartyTransactionGateway
import com.pissiphany.bany.domain.gateway.ThirdPartyTransactionGatewayFactory
import com.pissiphany.bany.adapter.dataStructure.YnabBudgetAccountIds

class ThirdPartyTransactionGatewayFactoryImpl(
    services: List<ThirdPartyTransactionService>, private val transactionMapper: YnabTransactionMapper
) : ThirdPartyTransactionGatewayFactory {
    private val idsToService: Map<YnabBudgetAccountIds, ThirdPartyTransactionService>

    init {
        val map = mutableMapOf<YnabBudgetAccountIds, ThirdPartyTransactionService>()
        for (service in services) {
            for (ids in service.getYnabBudgetAccountIds()) {
                map[ids] = service
            }
        }

        idsToService = map
    }

    override fun getGateway(budget: Budget, account: Account): ThirdPartyTransactionGateway {
        // TODO use mapper here!!!
        val budgetAccountIds = YnabBudgetAccountIds(ynabBudgetId = budget.id, ynabAccountId = account.id)
        val service = idsToService[budgetAccountIds]
            ?: throw NoSuitableTransactionGatewayException("unable to find gateway for: $budgetAccountIds")

        return ThirdPartyTransactionGatewayImpl(service, budgetAccountIds, transactionMapper)
    }

    class NoSuitableTransactionGatewayException(message: String) : Throwable(message)
}