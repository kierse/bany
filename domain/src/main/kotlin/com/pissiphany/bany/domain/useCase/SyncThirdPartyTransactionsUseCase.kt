package com.pissiphany.bany.domain.useCase

import com.pissiphany.bany.domain.dataStructure.*
import com.pissiphany.bany.domain.repository.ConfigurationRepository
import com.pissiphany.bany.shared.logger
import java.time.LocalDate
import java.time.OffsetDateTime

class SyncThirdPartyTransactionsUseCase(
    private val repo: ConfigurationRepository,
    private val getAccountDetails: Step1GetAccountDetails,
    private val newThirdPartyTransactions: Step2GetNewTransactions,
    private val processNewTransaction: Step3ProcessNewTransaction,
    private val saveTransactions: Step4SaveNewTransactions,
    private val outputBoundary: SyncThirdPartyTransactionsOutputBoundary
) {
    interface Step1GetAccountDetails {
        fun getAccountAndLastTransaction(budgetAccountIds: BudgetAccountIds): AccountAndTransaction
    }

    interface Step2GetNewTransactions {
        fun getTransactions(budgetAccountIds: BudgetAccountIds, date: LocalDate?): List<Transaction>
    }

    interface Step3ProcessNewTransaction {
        fun processTransaction(account: Account, newTransaction: Transaction): AccountTransaction
    }

    interface Step4SaveNewTransactions {
        fun saveTransactions(budgetAccountIds: BudgetAccountIds, transactions: List<AccountTransaction>)
    }

    private val logger by logger()

    fun sync() {
        val results = mutableListOf<SyncTransactionsResult>()
        for (budgetAccountIds in repo.getBudgetAccountIds()) {
            val (dateOfLastTransaction, transactions) = syncNewThirdPartyTransactions(budgetAccountIds)

            results.add(SyncTransactionsResult(budgetAccountIds, dateOfLastTransaction, transactions))
        }

        outputBoundary.present(results)
    }

    private fun syncNewThirdPartyTransactions(
        budgetAccountIds: BudgetAccountIds
    ): Pair<OffsetDateTime?, List<Transaction>> {
        val (account, lastTransaction) = getAccountDetails.getAccountAndLastTransaction(budgetAccountIds)

        val date = lastTransaction?.date
        val newTransactions = newThirdPartyTransactions.getTransactions(budgetAccountIds, date?.toLocalDate())
            .map { processNewTransaction.processTransaction(account, it) }
            .filter { it.amountInCents != 0 }
            .also { transactions ->
                logger.debug { "Saving the following to ${budgetAccountIds.name}: $transactions" }
                saveTransactions.saveTransactions(budgetAccountIds, transactions)
            }

        return Pair(date, newTransactions)
    }
}