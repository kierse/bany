package com.pissiphany.bany.adapter.factory

import com.pissiphany.bany.adapter.gateway.ThirdPartyTransactionGatewayImpl
import com.pissiphany.bany.adapter.mapper.YnabTransactionMapper
import com.pissiphany.bany.adapter.service.ThirdPartyTransactionService
import com.pissiphany.bany.domain.gateway.ThirdPartyTransactionGateway
import com.pissiphany.bany.domain.gateway.ThirdPartyTransactionGatewayFactory
import com.pissiphany.bany.adapter.dataStructure.YnabBudgetAccountIds
import com.pissiphany.bany.adapter.mapper.YnabBudgetAccountIdsMapper
import com.pissiphany.bany.domain.dataStructure.BudgetAccountIds

class ThirdPartyTransactionGatewayFactoryImpl(
    services: List<ThirdPartyTransactionService>,
    private val budgetAccountIdsMapper: YnabBudgetAccountIdsMapper,
    private val transactionMapper: YnabTransactionMapper
) : ThirdPartyTransactionGatewayFactory {
    private val idsToService: Map<YnabBudgetAccountIds, ThirdPartyTransactionService> by lazy {
        services
            .flatMap { service ->
                service
                    .getYnabBudgetAccountIds()
                    .map { id -> id to service }
            }
            .toMap()
    }

    override suspend fun getGateway(budgetAccountIds: BudgetAccountIds): ThirdPartyTransactionGateway {
        val ids = budgetAccountIdsMapper.toYnabBudgetAccountIds(budgetAccountIds)
        val service = checkNotNull(idsToService[ids]) { "Unable to find transaction gateway: $ids" }
        return ThirdPartyTransactionGatewayImpl(service, ids, transactionMapper)
    }
}