package com.pissiphany.bany.domain.useCase.step

import com.pissiphany.bany.domain.dataStructure.Account
import com.pissiphany.bany.domain.dataStructure.Budget
import com.pissiphany.bany.domain.dataStructure.Transaction
import com.pissiphany.bany.domain.gateway.ThirdPartyTransactionGatewayFactory
import com.pissiphany.bany.domain.useCase.SyncThirdPartyTransactionsUseCase
import java.time.LocalDate

class GetNewTransactions(
    private val gatewayFactory: ThirdPartyTransactionGatewayFactory
) : SyncThirdPartyTransactionsUseCase.Step3GetNewTransactions {

    override fun getTransactions(budget: Budget, account: Account, date: LocalDate?): List<Transaction> {
        val gateway = gatewayFactory.getGateway(budget, account)
        return gateway.getNewTransactionSince(date)
    }
}