package com.pissiphany.bany.adapter.mapper

import com.pissiphany.bany.adapter.dataStructure.YnabTransaction
import com.pissiphany.bany.domain.dataStructure.Account
import com.pissiphany.bany.domain.dataStructure.Transaction
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.time.OffsetDateTime

internal class YnabTransactionMapperTest {
    @Test
    fun toYnabTransaction() {
        val date = OffsetDateTime.now()
        val transaction = Transaction("transactionId", date, "payee", "memo", 100)
        val account = Account("accountId", "name", 50, false, Account.Type.CHECKING)

        assertEquals(
            YnabTransaction("transactionId", "accountId", date, "payee", "memo", 1000),
            YnabTransactionMapper().toYnabTransaction(transaction, account)
        )
    }

    @Test
    fun toTransaction() {
        val date = OffsetDateTime.now()
        val ynabTransaction = YnabTransaction("transactionId", "accountId", date, "payee", "memo", 1500)

        assertEquals(
            Transaction("transactionId", date, "payee", "memo", 150),
            YnabTransactionMapper().toTransaction(ynabTransaction)
        )
    }
}