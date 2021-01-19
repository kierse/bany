package com.pissiphany.bany.adapter.gateway

import com.pissiphany.bany.adapter.mapper.YnabAccountMapper
import com.pissiphany.bany.adapter.mapper.YnabBudgetAccountIdsMapper
import com.pissiphany.bany.adapter.service.YnabApiService
import com.pissiphany.bany.domain.dataStructure.Account
import com.pissiphany.bany.domain.dataStructure.BudgetAccountIds
import com.pissiphany.bany.domain.gateway.YnabBudgetAccountsGateway

class YnabBudgetAccountsGatewayImpl(
    private val service: YnabApiService,
    private val budgetAccountIdsMapper: YnabBudgetAccountIdsMapper,
    private val accountMapper: YnabAccountMapper
) : YnabBudgetAccountsGateway {
    override fun getAccount(budgetAccountIds: BudgetAccountIds): Account? {
        val ids = budgetAccountIdsMapper.toYnabBudgetAccountIds(budgetAccountIds)
        return service.getAccount(ids)
            ?.let { accountMapper.toAccount(it) }
    }
}