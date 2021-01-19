package com.pissiphany.bany.adapter.presenter

import com.pissiphany.bany.adapter.dataStructure.ViewModel
import com.pissiphany.bany.domain.dataStructure.AccountBalance
import com.pissiphany.bany.domain.dataStructure.AccountTransaction
import com.pissiphany.bany.domain.dataStructure.SyncTransactionsResult
import com.pissiphany.bany.domain.useCase.SyncThirdPartyTransactionsOutputBoundary
import java.math.BigDecimal
import java.time.format.DateTimeFormatter

class Presenter(private val view: View) : SyncThirdPartyTransactionsOutputBoundary {
    interface View {
        fun display(model: ViewModel)
    }

    override fun present(results: List<SyncTransactionsResult>) {
        val records = results.mapIndexed { index, result ->
            presentSyncAttempt(index, result)
        }

        view.display(ViewModel(records))
    }

    private fun presentSyncAttempt(index: Int, result: SyncTransactionsResult): ViewModel.Record {
        val date = result.dateOfLastTransaction
        val latestTransactionDate = if (date != null) {
            "Latest transaction date: ${date.format(DateTimeFormatter.ISO_LOCAL_DATE)}"
        } else {
            ""
        }

        val transactions = result.transactions.map(
            fun(transaction): String {
                val amount = BigDecimal(transaction.amountInCents).movePointLeft(2)
                val timestamp = transaction.date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                return when(transaction) {
                    is AccountTransaction -> "${if (amount < BigDecimal.ZERO) "CREDIT" else "DEBIT"} of $amount on $timestamp"
                    is AccountBalance -> "New balance of $amount on $timestamp"
                }
            }
        )

        return ViewModel.Record(
            syncIndex = "Sync ${index+1}",
            budgetId = "Budget ID:    ${result.budgetAccountIds.budgetId}",
            accountId = "Account ID:   ${result.budgetAccountIds.accountId}",
            accountName = "Account name: ${result.budgetAccountIds.name}",
            latestTransactionDate = latestTransactionDate,
            numberOfTransactionsFound = "Found ${result.transactions.size} new transaction(s)",
            transactions = transactions
        )
    }
}