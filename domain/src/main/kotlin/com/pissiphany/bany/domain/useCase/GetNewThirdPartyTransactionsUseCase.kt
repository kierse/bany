package com.pissiphany.bany.domain.useCase

import com.pissiphany.bany.domain.dataStructure.Account
import com.pissiphany.bany.domain.dataStructure.Transaction
import com.pissiphany.bany.domain.gateway.ThirdPartyTransactionGateway
import java.time.LocalTime

class GetNewThirdPartyTransactionsUseCase(transactionGateways: List<ThirdPartyTransactionGateway>) {
    private val gatewayByAccount: Map<Account, ThirdPartyTransactionGateway> =
        transactionGateways.associateBy { it.account }

    fun getNewTransactions(account: Account, date: LocalTime?): List<Transaction> {
        val gateway = gatewayByAccount[account] ?: return emptyList()

        return gateway.getNewTransactionSince(date)
    }
}