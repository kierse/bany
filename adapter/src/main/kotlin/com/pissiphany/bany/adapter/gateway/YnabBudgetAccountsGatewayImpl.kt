package com.pissiphany.bany.adapter.gateway

import com.pissiphany.bany.adapter.mapper.AccountMapper
import com.pissiphany.bany.adapter.mapper.BudgetMapper
import com.pissiphany.bany.adapter.service.YnabService
import com.pissiphany.bany.domain.dataStructure.Account
import com.pissiphany.bany.domain.dataStructure.Budget
import com.pissiphany.bany.domain.gateway.YnabBudgetAccountsGateway

class YnabBudgetAccountsGatewayImpl(
    private val ynabService: YnabService,
    private val budgetMapper: BudgetMapper,
    private val accountMapper: AccountMapper
) : YnabBudgetAccountsGateway {
    override fun getBudget(budgetId: String): Budget? {
        val call = ynabService.getBudget(budgetId)
        val response = call.execute()
        val ynabBudgetWrapper = response.body() ?: return null

        return budgetMapper.toBudget(ynabBudgetWrapper.ynabBudget)
    }

    override fun getAccount(budgetId: String, accountId: String): Account? {
        val call = ynabService.getAccount(budgetId, accountId)
        val response = call.execute()
        val ynabAccount = response.body() ?: return null

        return accountMapper.toAccount(ynabAccount)
    }
}