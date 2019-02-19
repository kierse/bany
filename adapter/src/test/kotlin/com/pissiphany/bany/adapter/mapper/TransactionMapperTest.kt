package com.pissiphany.bany.adapter.mapper

import com.pissiphany.bany.adapter.dataStructure.YnabTransaction
import com.pissiphany.bany.domain.dataStructure.Account
import com.pissiphany.bany.domain.dataStructure.Transaction
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.time.LocalTime

internal class TransactionMapperTest {
    @Test
    fun toYnabTransaction() {
        val date = LocalTime.now()
        val transaction = Transaction("transactionId", date, 10L)
        val account = Account("accountId", "name", 5L, false, Account.Type.CHECKING)

        assertEquals(
            YnabTransaction("transactionId", "accountId", 10L, date),
            TransactionMapper().toYnabTransaction(transaction, account)
        )
    }
}