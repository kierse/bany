package com.pissiphany.bany.adapter.factory

import com.pissiphany.bany.adapter.gateway.ThirdPartyTransactionGatewayImpl
import com.pissiphany.bany.adapter.mapper.BanyPluginTransactionMapper
import com.pissiphany.bany.domain.dataStructure.Account
import com.pissiphany.bany.domain.dataStructure.Budget
import com.pissiphany.bany.domain.gateway.ThirdPartyTransactionGateway
import com.pissiphany.bany.domain.gateway.ThirdPartyTransactionGatewayFactory
import com.pissiphany.bany.plugin.BanyPlugin
import com.pissiphany.bany.plugin.dataStructure.YnabBudgetAccountIds

class ThirdPartyTransactionGatewayFactoryImpl(
    plugins: List<BanyPlugin>, private val transactionMapper: BanyPluginTransactionMapper
) : ThirdPartyTransactionGatewayFactory {
    private val idsToPlugin: Map<YnabBudgetAccountIds, BanyPlugin>

    init {
        val map = mutableMapOf<YnabBudgetAccountIds, BanyPlugin>()
        for (plugin in plugins) {
            for (ids in plugin.getYnabBudgetAccountIds()) {
                map[ids] = plugin
            }
        }

        idsToPlugin = map
    }

    override fun getGateway(budget: Budget, account: Account): ThirdPartyTransactionGateway {
        val budgetAccountIds = YnabBudgetAccountIds(ynabBudgetId = budget.id, ynabAccountId = account.id)
        val plugin = idsToPlugin[budgetAccountIds]
            ?: throw NoSuitableTransactionGatewayException(
                "unable to find gateway for: budgetId=${budget.id}, accountId=${account.id}"
            )

        return ThirdPartyTransactionGatewayImpl(plugin, budgetAccountIds, transactionMapper)
    }

    class NoSuitableTransactionGatewayException(message: String) : Throwable(message)
}