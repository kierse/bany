package com.pissiphany.bany.domain.useCase.step

import com.pissiphany.bany.domain.dataStructure.Account
import com.pissiphany.bany.domain.dataStructure.Transaction
import com.pissiphany.bany.domain.gateway.ThirdPartyTransactionGateway
import com.pissiphany.bany.domain.useCase.SyncThirdPartyTransactionsUseCase
import java.time.LocalDate

class GetNewTransactions(
    transactionGateways: List<ThirdPartyTransactionGateway>
) : SyncThirdPartyTransactionsUseCase.Step3GetNewTransactions {
    private val gatewayByAccount: Map<String, ThirdPartyTransactionGateway> =
        transactionGateways.associateBy { it.accountId }

    override fun getTransactions(account: Account, date: LocalDate?): List<Transaction> {
        val gateway = gatewayByAccount[account.id] ?: return emptyList()
        return gateway.getNewTransactionSince(date)
    }
}