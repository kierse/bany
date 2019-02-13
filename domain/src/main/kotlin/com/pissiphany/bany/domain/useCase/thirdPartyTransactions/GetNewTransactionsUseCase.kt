package com.pissiphany.bany.domain.useCase.thirdPartyTransactions

import com.pissiphany.bany.domain.dataStructure.Account
import com.pissiphany.bany.domain.gateway.ThirdPartyTransactionGateway

class GetNewTransactionsUseCase(transactionGateways: List<ThirdPartyTransactionGateway>) {
    private val gatewayByAccount: Map<Account, ThirdPartyTransactionGateway> =
        transactionGateways.associateBy { it.account }

    fun run(input: GetNewTransactionsInputBoundary, output: GetNewTransactionsOutputBoundary) {
        if (input.account !in gatewayByAccount) return

        output.transactions = gatewayByAccount.getValue(input.account).getNewTransactionSince(input.date)
    }
}