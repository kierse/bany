package com.pissiphany.bany.adapter.gateway

import com.pissiphany.bany.adapter.mapper.YnabTransactionMapper
import com.pissiphany.bany.adapter.service.ThirdPartyTransactionService
import com.pissiphany.bany.domain.dataStructure.Transaction
import com.pissiphany.bany.domain.gateway.ThirdPartyTransactionGateway
import com.pissiphany.bany.adapter.dataStructure.YnabBudgetAccountIds
import java.time.LocalDate

class ThirdPartyTransactionGatewayImpl(
    private val service: ThirdPartyTransactionService,
    private val budgetAccountIds: YnabBudgetAccountIds,
    private val mapper: YnabTransactionMapper
) : ThirdPartyTransactionGateway {
    // TODO test
    override suspend fun getNewTransactionSince(date: LocalDate?): List<Transaction> {
        return service
            .getNewYnabTransactionsSince(budgetAccountIds, date)
            .map { mapper.toTransaction(it) }
    }
}