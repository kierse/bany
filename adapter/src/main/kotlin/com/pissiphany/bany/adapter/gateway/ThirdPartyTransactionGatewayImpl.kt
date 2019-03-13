package com.pissiphany.bany.adapter.gateway

import com.pissiphany.bany.adapter.mapper.BanyPluginTransactionMapper
import com.pissiphany.bany.plugin.BanyPlugin
import com.pissiphany.bany.domain.dataStructure.Transaction
import com.pissiphany.bany.domain.gateway.ThirdPartyTransactionGateway
import com.pissiphany.bany.plugin.dataStructure.YnabBudgetAccountIds
import java.time.LocalDate

class ThirdPartyTransactionGatewayImpl(
    private val plugin: BanyPlugin,
    private val budgetAccountIds: YnabBudgetAccountIds,
    private val mapper: BanyPluginTransactionMapper
) : ThirdPartyTransactionGateway {
    // TODO test
    override fun getNewTransactionSince(date: LocalDate?): List<Transaction> {
        return plugin
            .getNewBanyPluginTransactionsSince(budgetAccountIds, date)
            .map { mapper.toTransaction(it) }
    }
}