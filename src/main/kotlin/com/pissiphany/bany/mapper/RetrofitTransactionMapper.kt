package com.pissiphany.bany.mapper

import com.pissiphany.bany.adapter.dataStructure.YnabAccount
import com.pissiphany.bany.adapter.dataStructure.YnabTransaction
import com.pissiphany.bany.dataStructure.RetrofitAccount
import com.pissiphany.bany.dataStructure.RetrofitTransaction

class RetrofitTransactionMapper {
    fun toYnabTransaction(transaction: RetrofitTransaction, account: YnabAccount): YnabTransaction {
        return YnabTransaction(
            id = transaction.id,
            accountId = account.id,
            amount = transaction.amount,
            date = transaction.date
        )
    }

    fun toRetrofitTransaction(transaction: YnabTransaction): RetrofitTransaction {
        return RetrofitTransaction(
            id = transaction.id,
            amount = transaction.amount,
            date = transaction.date,
            account_id = transaction.accountId
        )
    }
}