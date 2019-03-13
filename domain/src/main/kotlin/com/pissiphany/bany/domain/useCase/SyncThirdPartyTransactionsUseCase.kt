package com.pissiphany.bany.domain.useCase

import com.pissiphany.bany.domain.dataStructure.*
import java.time.LocalDate

class SyncThirdPartyTransactionsUseCase(
    private val budgetAccounts: Step1GetBudgetAccounts,
    private val mostRecentTransaction: Step2GetMostRecentTransaction,
    private val newTransactions: Step3GetNewTransactions,
    private val saveTransactions: Step4SaveNewTransactions,
    private val outputBoundary: SyncThirdPartyTransactionsOutputBoundary
) {
    interface Step1GetBudgetAccounts {
        fun getBudgetAccounts(): List<BudgetAccount>
    }

    interface Step2GetMostRecentTransaction {
        fun getTransaction(budget: Budget, account: Account): Transaction?
    }

    interface Step3GetNewTransactions {
        fun getTransactions(budget: Budget, account: Account, date: LocalDate?): List<Transaction>
    }

    interface Step4SaveNewTransactions {
        fun saveTransactions(budget: Budget, account: Account, transactions: List<Transaction>)
    }

    fun sync() {
        val results = mutableListOf<SyncTransactionsResult>()
        for ((budget, account) in budgetAccounts.getBudgetAccounts()) {
            val (dateOfLastTransaction, transactions) = syncNewThirdPartyTransactions(budget, account)

            results.add(SyncTransactionsResult(budget, account, dateOfLastTransaction, transactions))
        }

        outputBoundary.present(results)
    }

    private fun syncNewThirdPartyTransactions(
        budget: Budget, account: Account
    ): Pair<LocalDate?, List<Transaction>> {
        val transaction = mostRecentTransaction.getTransaction(budget, account)

        val date = transaction?.date
        val newTransactions = newTransactions.getTransactions(budget, account, date)

        saveTransactions.saveTransactions(budget, account, newTransactions)

        return Pair(date, newTransactions)
    }
}