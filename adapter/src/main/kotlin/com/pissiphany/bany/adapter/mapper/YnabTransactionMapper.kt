package com.pissiphany.bany.adapter.mapper

import com.pissiphany.bany.adapter.dataStructure.YnabTransaction
import com.pissiphany.bany.domain.dataStructure.Account
import com.pissiphany.bany.domain.dataStructure.Transaction

class YnabTransactionMapper {
    fun toYnabTransaction(transaction: Transaction, account: Account): YnabTransaction {
        return YnabTransaction(
            id = transaction.id,
            accountId =  account.id,
            date = transaction.date,
            payee = transaction.payee,
            memo = transaction.memo,
            amountInMilliUnits = transaction.amountInCents * 10
        )
    }

    fun toTransaction(ynabTransaction: YnabTransaction): Transaction {
        return Transaction(
            id = ynabTransaction.id,
            date = ynabTransaction.date,
            payee = ynabTransaction.payee,
            memo = ynabTransaction.memo,
            amountInCents = ynabTransaction.amountInMilliUnits / 10
        )
    }
}