package com.pissiphany.bany.adapter.mapper

import com.pissiphany.bany.adapter.dataStructure.YnabTransaction
import com.pissiphany.bany.domain.dataStructure.Account
import com.pissiphany.bany.domain.dataStructure.Transaction

class TransactionMapper {
    fun toYnabTransaction(transaction: Transaction, account: Account): YnabTransaction {
        return YnabTransaction(
            id = transaction.id,
            accountId = account.id,
            amount = transaction.amount,
            date = transaction.date
        )
    }
}