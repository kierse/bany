package com.pissiphany.bany.adapter.gateway

import com.pissiphany.bany.adapter.mapper.YnabAccountMapper
import com.pissiphany.bany.adapter.mapper.YnabBudgetMapper
import com.pissiphany.bany.adapter.service.YnabApiService
import com.pissiphany.bany.domain.dataStructure.Account
import com.pissiphany.bany.domain.dataStructure.Budget
import com.pissiphany.bany.domain.gateway.YnabBudgetAccountsGateway

class YnabBudgetAccountsGatewayImpl(
    private val service: YnabApiService,
    private val budgetMapper: YnabBudgetMapper,
    private val accountMapper: YnabAccountMapper
) : YnabBudgetAccountsGateway {
    override fun getBudget(budgetId: String): Budget? {
        return service.getBudget(budgetId)
            ?.let { budgetMapper.toBudget(it) }
    }

    override fun getAccount(budgetId: String, accountId: String): Account? {
        return service.getAccount(budgetId, accountId)
            ?.let { accountMapper.toAccount(it) }
    }
}