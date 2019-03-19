package com.pissiphany.bany.mapper

import com.pissiphany.bany.adapter.dataStructure.YnabAccount
import com.pissiphany.bany.adapter.dataStructure.YnabTransaction
import com.pissiphany.bany.dataStructure.RetrofitTransaction

class RetrofitTransactionMapper {
    fun toYnabTransaction(transaction: RetrofitTransaction, account: YnabAccount): YnabTransaction {
        return YnabTransaction(
            id = transaction.id,
            accountId = account.id,
            date = transaction.date,
            payee = transaction.payee_name,
            memo = transaction.memo,
            amountInMilliUnits = transaction.amount
        )
    }

    fun toRetrofitTransaction(transaction: YnabTransaction): RetrofitTransaction {
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