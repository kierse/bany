package com.pissiphany.bany.domain.useCase.step

import com.pissiphany.bany.domain.dataStructure.Account
import com.pissiphany.bany.domain.dataStructure.AccountBalance
import com.pissiphany.bany.domain.dataStructure.AccountTransaction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime

class ProcessNewTransactionTest {
    @Test
    fun `processTransaction - account transaction`() {
        val account = Account(
            id = "id", name = "name", balanceInCents = 0, closed = false, type = Account.Type.CHECKING
        )
        val transaction = AccountTransaction(
            id = "id", date = OffsetDateTime.now(), payee = "payee", memo = "memo", amountInCents = 0
        )

        val result = ProcessNewTransaction().processTransaction(account, transaction)

        assertEquals(transaction, result)
    }

    @Test
    fun `processTransaction - account balance`() {
        val account = Account(
            id = "id", name = "name", balanceInCents = 0, closed = false, type = Account.Type.CHECKING
        )
        val transaction = AccountBalance(
            date = OffsetDateTime.now(), payee = "payee", amountInCents = 0
        )
        val expected = AccountTransaction(
            id = null,
            date = transaction.date,
            payee = transaction.payee,
            memo = "",
            amountInCents = transaction.amountInCents
        )

        val result = ProcessNewTransaction().processTransaction(account, transaction)

        assertEquals(expected, result)
    }
}