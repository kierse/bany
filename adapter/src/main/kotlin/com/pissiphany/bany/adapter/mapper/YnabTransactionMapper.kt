package com.pissiphany.bany.adapter.mapper

import com.pissiphany.bany.adapter.dataStructure.YnabAccountBalance
import com.pissiphany.bany.adapter.dataStructure.YnabAccountTransaction
import com.pissiphany.bany.adapter.dataStructure.YnabBudgetAccountIds
import com.pissiphany.bany.adapter.dataStructure.YnabTransaction
import com.pissiphany.bany.domain.dataStructure.AccountBalance
import com.pissiphany.bany.domain.dataStructure.AccountTransaction
import com.pissiphany.bany.domain.dataStructure.Transaction

class YnabTransactionMapper {
    fun toYnabTransaction(budgetAccountIds: YnabBudgetAccountIds, transaction: Transaction): YnabTransaction {
        return when (transaction) {
            is AccountTransaction -> toYnabAccountTransaction(budgetAccountIds, transaction)
            is AccountBalance -> toYnabAccountBalance(budgetAccountIds, transaction)
        }
    }

    fun toYnabAccountTransaction(
        budgetAccountIds: YnabBudgetAccountIds, accountTransaction: AccountTransaction
    ): YnabAccountTransaction {
        return YnabAccountTransaction(
            id = null,
            memo = accountTransaction.memo,
            accountId = budgetAccountIds.ynabAccountId,
            date = accountTransaction.date,
            payee = accountTransaction.payee,
            amountInMilliUnits = accountTransaction.amountInCents.toMilliUnits()
        )
    }

    private fun toYnabAccountBalance(
        budgetAccountIds: YnabBudgetAccountIds, accountBalance: AccountBalance
    ): YnabAccountBalance {
        return YnabAccountBalance(
            accountId = budgetAccountIds.ynabAccountId,
            date = accountBalance.date,
            payee = accountBalance.payee,
            amountInMilliUnits = accountBalance.amountInCents.toMilliUnits()
        )
    }

    fun toTransaction(ynabTransaction: YnabTransaction): Transaction {
        return when(ynabTransaction) {
            is YnabAccountTransaction -> toAccountTransaction(ynabTransaction)
            is YnabAccountBalance -> toAccountBalance(ynabTransaction)
        }
    }

    fun toAccountTransaction(accountTransaction: YnabAccountTransaction): AccountTransaction {
        return AccountTransaction(
            id = accountTransaction.id,
            memo = accountTransaction.memo,
            date = accountTransaction.date,
            payee = accountTransaction.payee,
            amountInCents = accountTransaction.amountInMilliUnits.toCents()
        )
    }

    private fun toAccountBalance(accountBalance: YnabAccountBalance): AccountBalance {
        return AccountBalance(
            date = accountBalance.date,
            payee = accountBalance.payee,
            amountInCents = accountBalance.amountInMilliUnits.toCents()
        )
    }

    private fun Int.toMilliUnits() = this * 10
    private fun Int.toCents() = this / 10
}