package com.pissiphany.bany.domain.useCase.step

import com.pissiphany.bany.domain.dataStructure.AccountTransaction
import com.pissiphany.bany.domain.dataStructure.BudgetAccountIds
import com.pissiphany.bany.domain.gateway.YnabSaveTransactionsGateway
import com.pissiphany.bany.domain.useCase.SyncThirdPartyTransactionsUseCase

class SaveNewTransactions(
    private val ynabGateway: YnabSaveTransactionsGateway
) : SyncThirdPartyTransactionsUseCase.Step4SaveNewTransactions {
    override suspend fun saveTransactions(budgetAccountIds: BudgetAccountIds, transactions: List<AccountTransaction>) {
        ynabGateway.saveTransactions(budgetAccountIds, transactions)
    }
}