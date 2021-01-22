package com.pissiphany.bany.adapter.mapper

import com.pissiphany.bany.adapter.dataStructure.YnabAccountBalance
import com.pissiphany.bany.adapter.dataStructure.YnabAccountTransaction
import com.pissiphany.bany.adapter.dataStructure.YnabBudgetAccountIds
import com.pissiphany.bany.adapter.dataStructure.YnabTransaction
import com.pissiphany.bany.domain.dataStructure.AccountBalance
import com.pissiphany.bany.domain.dataStructure.AccountTransaction
import com.pissiphany.bany.domain.dataStructure.Transaction
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.time.OffsetDateTime

internal class YnabTransactionMapperTest {
    @Test
    fun `toYnabTransaction - account transaction`() {
        val date = OffsetDateTime.now()
        val transaction = AccountTransaction("transactionId", date, "payee", "memo", 100)
        val ids = YnabBudgetAccountIds(ynabBudgetId = "budgetId", ynabAccountId = "accountId")

        assertEquals(
            YnabAccountTransaction("transactionId", "accountId", date, "payee", "memo", 1000),
            YnabTransactionMapper().toYnabTransaction(ids, transaction)
        )
    }

    @Test
    fun `toYnabTransaction - account balance`() {
        val date = OffsetDateTime.now()
        val transaction = AccountBalance(date, "payee", 100)
        val ids = YnabBudgetAccountIds(ynabBudgetId = "budgetId", ynabAccountId = "accountId")

        assertEquals(
            YnabAccountBalance("accountId", date, "payee", 1000),
            YnabTransactionMapper().toYnabTransaction(ids, transaction)
        )
    }

    @Test
    fun `toTransaction - account transaction`() {
        val date = OffsetDateTime.now()
        val ynabTransaction = YnabAccountTransaction("transactionId", "accountId", date, "payee", "memo", 1500)

        assertEquals(
            AccountTransaction("transactionId", date, "payee", "memo", 150),
            YnabTransactionMapper().toTransaction(ynabTransaction)
        )
    }

    @Test
    fun `toTransaction - account balance`() {
        val date = OffsetDateTime.now()
        val ynabTransaction = YnabAccountBalance("accountId", date, "payee", 1500)

        assertEquals(
            AccountBalance(date, "payee", 150),
            YnabTransactionMapper().toTransaction(ynabTransaction)
        )
    }
}