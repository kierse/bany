package com.pissiphany.bany.adapter.gateway

import com.pissiphany.bany.adapter.mapper.BanyPluginTransactionMapper
import com.pissiphany.bany.plugin.BanyPlugin
import com.pissiphany.bany.domain.dataStructure.Transaction
import com.pissiphany.bany.domain.gateway.ThirdPartyTransactionGateway
import java.time.LocalDate

class ThirdPartyTransactionGatewayImpl(
    private val plugin: BanyPlugin, private val mapper: BanyPluginTransactionMapper
) : ThirdPartyTransactionGateway {
    override val accountId: String get() = plugin.getYnabAccountId()

    // TODO test
    override fun getNewTransactionSince(date: LocalDate?): List<Transaction> {
        return plugin
            .getNewTransactionsSince(date)
            .map { mapper.toTransaction(it) }
    }
}