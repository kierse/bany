package com.pissiphany.bany.domain.useCase

import com.pissiphany.bany.domain.dataStructure.*
import com.pissiphany.bany.domain.repository.ConfigurationRepository
import com.pissiphany.bany.shared.logger
import kotlinx.coroutines.*
import java.time.LocalDate

class SyncThirdPartyTransactionsUseCase(
    private val repo: ConfigurationRepository,
    private val getAccountDetails: Step1GetAccountDetails,
    private val newThirdPartyTransactions: Step2GetNewTransactions,
    private val processNewTransaction: Step3ProcessNewTransaction,
    private val saveTransactions: Step4SaveNewTransactions,
    private val outputBoundary: SyncThirdPartyTransactionsOutputBoundary
) {
    interface Step1GetAccountDetails {
        suspend fun getAccountAndLastTransaction(budgetAccountIds: BudgetAccountIds): AccountAndTransaction
    }

    interface Step2GetNewTransactions {
        suspend fun getTransactions(budgetAccountIds: BudgetAccountIds, date: LocalDate?): List<Transaction>
    }

    interface Step3ProcessNewTransaction {
        fun processTransaction(account: Account, newTransaction: Transaction): AccountTransaction
    }

    interface Step4SaveNewTransactions {
        suspend fun saveTransactions(budgetAccountIds: BudgetAccountIds, transactions: List<AccountTransaction>)
    }

    private val logger by logger()

    suspend fun sync() {
        val deferredResults = supervisorScope {
            repo.getBudgetAccountIds()
                .map { budgetAccountIds ->
                    async {
                        val (account, lastTransaction) = getAccountDetails.getAccountAndLastTransaction(budgetAccountIds)

                        newThirdPartyTransactions
                            .getTransactions(
                                budgetAccountIds,
                                lastTransaction?.date?.toLocalDate()
                            )
                            .map { processNewTransaction.processTransaction(account, it) }
                            .filter { it.amountInCents != 0 }
                            .takeIf(List<AccountTransaction>::isNotEmpty)
                            ?.also { transactions ->
                                logger.debug { "Saving the following to ${budgetAccountIds.name}: $transactions" }
                                saveTransactions.saveTransactions(budgetAccountIds, transactions)
                            }
                            ?.let { transactions ->
                                SyncTransactionsResult(budgetAccountIds, lastTransaction?.date, transactions)
                            }
                    }
                }
        }

        val results = deferredResults.mapNotNull { deferred ->
            try {
                deferred.await()
            } catch (e: Throwable) {
                logger.error("Error encountered while syncing: ${e.message}")
                null
            }
        }

        outputBoundary.present(results)
    }
}