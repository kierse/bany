package com.pissiphany.bany.adapter.controller

import com.pissiphany.bany.adapter.boundary.*
import com.pissiphany.bany.domain.useCase.linkedAccounts.GetLinkedAccountsUseCase
import com.pissiphany.bany.domain.useCase.ynabTransactions.GetMostRecentUseCase
import com.pissiphany.bany.domain.useCase.thirdPartyTransactions.GetNewTransactionsUseCase
import com.pissiphany.bany.domain.useCase.ynabTransactions.SaveTransactionsUseCase
import java.time.LocalTime

class SyncTransactionsWithYnabController(
    private val linkedAccountsUseCase: GetLinkedAccountsUseCase,
    private val recentTransactionUseCase: GetMostRecentUseCase,
    private val newTransactionsUseCase: GetNewTransactionsUseCase,
    private val saveTransactionsUseCase: SaveTransactionsUseCase
) {
    fun sync() {
        val linkedAccountsOutput = GetLinkedAccountsOutputBoundaryImpl()
        linkedAccountsUseCase.run(linkedAccountsOutput)

        for ((budget, account) in linkedAccountsOutput.linkedAccounts) {
            val recentTransactionsInput = GetMostRecentInputBoundaryImpl(budget, account)
            val recentTransactionsOutput = GetMostRecentOutputBoundaryImpl()

            recentTransactionUseCase.run(recentTransactionsInput, recentTransactionsOutput)
            val date: LocalTime? = recentTransactionsOutput.transaction?.date

            val newTransactionsInputBoundary = GetNewTransactionsInputBoundarImpl(account, date)
            val newTransactionsOutputBoundary = GetNewTransactionsOutputBoundaryImpl()

            newTransactionsUseCase.run(newTransactionsInputBoundary, newTransactionsOutputBoundary)
            if (newTransactionsOutputBoundary.transactions.isEmpty()) continue

            val saveTransactionsInputBoundary = SaveTransactionsInputBoundaryImpl(
                budget, account, newTransactionsOutputBoundary.transactions
            )
            saveTransactionsUseCase.run(saveTransactionsInputBoundary)
        }
    }
}