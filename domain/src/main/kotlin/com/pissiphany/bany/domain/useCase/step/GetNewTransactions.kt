package com.pissiphany.bany.domain.useCase.step

import com.pissiphany.bany.domain.dataStructure.BudgetAccountIds
import com.pissiphany.bany.domain.dataStructure.Transaction
import com.pissiphany.bany.domain.gateway.ThirdPartyTransactionGatewayFactory
import com.pissiphany.bany.domain.useCase.SyncThirdPartyTransactionsUseCase
import java.time.LocalDate

class GetNewTransactions(
    private val gatewayFactory: ThirdPartyTransactionGatewayFactory
) : SyncThirdPartyTransactionsUseCase.Step2GetNewTransactions {

    override fun getTransactions(budgetAccountIds: BudgetAccountIds, date: LocalDate?): List<Transaction> {
        val gateway = gatewayFactory.getGateway(budgetAccountIds)
        return gateway.getNewTransactionSince(date)
    }
}