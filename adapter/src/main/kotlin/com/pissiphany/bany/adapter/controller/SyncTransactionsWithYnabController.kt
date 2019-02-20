package com.pissiphany.bany.adapter.controller

import com.pissiphany.bany.adapter.boundary.*
import com.pissiphany.bany.domain.dataStructure.Account
import com.pissiphany.bany.domain.dataStructure.Budget
import com.pissiphany.bany.domain.dataStructure.Transaction
import com.pissiphany.bany.domain.useCase.budgetAccounts.GetBudgetAccountsUseCase
import com.pissiphany.bany.domain.useCase.ynabTransactions.GetMostRecentUseCase
import com.pissiphany.bany.domain.useCase.thirdPartyTransactions.GetNewTransactionsUseCase
import com.pissiphany.bany.domain.useCase.ynabTransactions.SaveTransactionsUseCase
import java.time.LocalDate

class SyncTransactionsWithYnabController(
    private val budgetAccountsUseCase: GetBudgetAccountsUseCase,
    private val recentTransactionUseCase: GetMostRecentUseCase,
    private val newTransactionsUseCase: GetNewTransactionsUseCase,
    private val saveTransactionsUseCase: SaveTransactionsUseCase
) {
    fun sync() {
        val budgetAccountsOutput = GetBudgetAccountsOutputBoundaryImpl()
        budgetAccountsUseCase.run(budgetAccountsOutput)

        for ((budget, account) in budgetAccountsOutput.budgetAccounts) {
            syncNewAccountTransactions(budget, account)
        }
    }

    private fun syncNewAccountTransactions(budget: Budget, account: Account) {
        val date = fetchDateOfMostRecentYnabTransaction(budget, account)

        val newTransactions = fetchNewThirdPartyTransactions(account, date)
        if (newTransactions.isEmpty()) return

        saveNewTransactionsToYnab(budget, account, newTransactions)
    }

    private fun fetchDateOfMostRecentYnabTransaction(budget: Budget, account: Account): LocalDate? {
        val recentTransactionsInput = GetMostRecentInputBoundaryImpl(budget, account)
        val recentTransactionsOutput = GetMostRecentOutputBoundaryImpl()

        recentTransactionUseCase.run(recentTransactionsInput, recentTransactionsOutput)
        return recentTransactionsOutput.transaction?.date
    }

    private fun fetchNewThirdPartyTransactions(account: Account, date: LocalDate?): List<Transaction> {
        val newTransactionsInputBoundary = GetNewTransactionsInputBoundaryImpl(account, date)
        val newTransactionsOutputBoundary = GetNewTransactionsOutputBoundaryImpl()

        newTransactionsUseCase.run(newTransactionsInputBoundary, newTransactionsOutputBoundary)
        return newTransactionsOutputBoundary.transactions
    }

    private fun saveNewTransactionsToYnab(budget: Budget, account: Account, newTransactions: List<Transaction>) {
        val saveTransactionsInputBoundary = SaveTransactionsInputBoundaryImpl(
            budget, account, newTransactions
        )
        saveTransactionsUseCase.run(saveTransactionsInputBoundary)
    }
}