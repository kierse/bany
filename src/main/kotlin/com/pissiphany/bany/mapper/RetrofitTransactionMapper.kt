package com.pissiphany.bany.mapper

import com.pissiphany.bany.adapter.dataStructure.YnabAccountTransaction
import com.pissiphany.bany.adapter.dataStructure.YnabBudgetAccountIds
import com.pissiphany.bany.dataStructure.RetrofitTransaction

class RetrofitTransactionMapper {
    fun toYnabTransaction(
        budgetAccountIds: YnabBudgetAccountIds, transaction: RetrofitTransaction
    ): YnabAccountTransaction {
        return YnabAccountTransaction(
            id = transaction.id,
            accountId = budgetAccountIds.ynabAccountId,
            date = transaction.date,
            payee = transaction.payee_name,
            memo = transaction.memo,
            amountInMilliUnits = transaction.amount
        )
    }

    fun toRetrofitTransaction(transaction: YnabAccountTransaction): RetrofitTransaction {
        return RetrofitTransaction(
            id = transaction.id,
            account_id = transaction.accountId,
            date = transaction.date,
            payee_name = transaction.payee,
            memo = transaction.memo,
            amount = transaction.amountInMilliUnits
        )
    }
}