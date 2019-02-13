package com.pissiphany.bany.adapter.controller

import com.pissiphany.bany.domain.dataStructure.Transaction
import com.pissiphany.bany.domain.useCase.MonitoredYnabAccountsUseCase
import com.pissiphany.bany.domain.useCase.GetMostRecentYnabTransactionUseCase
import com.pissiphany.bany.domain.useCase.GetNewThirdPartyTransactionsUseCase
import com.pissiphany.bany.domain.useCase.SaveTransactionsToYnabUseCase
import java.time.LocalTime

class SyncTransactionsWithYnabController(
    private val monitoredAccountsUseCase: MonitoredYnabAccountsUseCase,
    private val recentTransactionUseCase: GetMostRecentYnabTransactionUseCase,
    private val newTransactionsUseCase: GetNewThirdPartyTransactionsUseCase,
    private val saveTransactionsToYnabUseCase: SaveTransactionsToYnabUseCase
) {
    fun sync() {
        for ((budget, account) in monitoredAccountsUseCase.getAccountsToQuery()) {
            val lastTransaction: Transaction? = recentTransactionUseCase.getMostRecentTransaction(budget, account)
            val date: LocalTime? = lastTransaction?.date

            val newTransactions = newTransactionsUseCase.getNewTransactions(account, date)
            if (newTransactions.isEmpty()) continue

            saveTransactionsToYnabUseCase.saveTransactions(budget, account, newTransactions)
        }
    }
}