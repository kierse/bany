package com.pissiphany.bany.domain.useCase.step

import com.pissiphany.bany.domain.dataStructure.Account
import com.pissiphany.bany.domain.dataStructure.Budget
import com.pissiphany.bany.domain.dataStructure.Transaction
import com.pissiphany.bany.domain.gateway.YnabSaveTransactionsGateway
import com.pissiphany.bany.domain.useCase.SyncThirdPartyTransactionsUseCase

class SaveNewTransactions(
    private val ynabGateway: YnabSaveTransactionsGateway
) : SyncThirdPartyTransactionsUseCase.Step4SaveNewTransactions {
    override fun saveTransactions(budget: Budget, account: Account, transactions: List<Transaction>) {
        ynabGateway.saveTransactions(budget, account, transactions)
    }
}