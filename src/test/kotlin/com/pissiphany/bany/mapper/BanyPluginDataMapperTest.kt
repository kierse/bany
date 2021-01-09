package com.pissiphany.bany.mapper

import com.pissiphany.bany.adapter.dataStructure.YnabTransaction
import com.pissiphany.bany.plugin.dataStructure.BanyPluginTransaction
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import java.lang.IllegalArgumentException
import java.math.BigDecimal
import java.time.OffsetDateTime

internal class BanyPluginDataMapperTest {
    @Test
    fun toTransaction__debit() {
        val transaction = YnabTransaction(null, "accountId", OffsetDateTime.now(), "payee", "memo", -10000)
        val pluginTransaction = BanyPluginTransaction(
            date = transaction.date,
            payee = transaction.payee,
            memo = transaction.memo,
            debit = BigDecimal("10.00"),
            credit = BigDecimal.ZERO
        )

        assertEquals(transaction, BanyPluginDataMapper().toYnabTransaction(pluginTransaction, "accountId"))
    }

    @Test
    fun toTransaction__credit() {
        val transaction = YnabTransaction(null, "accountId", OffsetDateTime.now(), "payee", "memo", 1500)
        val pluginTransaction = BanyPluginTransaction(
            date = transaction.date,
            payee = transaction.payee,
            memo = transaction.memo,
            credit = BigDecimal("1.50"),
            debit = BigDecimal.ZERO
        )

        assertEquals(transaction, BanyPluginDataMapper().toYnabTransaction(pluginTransaction, "accountId"))
    }

    @Test
    fun toTransaction__debit_and_credit() {
        val pluginTransaction = BanyPluginTransaction(
            date = OffsetDateTime.now(),
            payee = "payee",
            memo = "memo",
            credit = BigDecimal("1.50"),
            debit = BigDecimal("10.00")
        )

        assertThrows<IllegalArgumentException> {
            BanyPluginDataMapper().toYnabTransaction(pluginTransaction, "accountId")
        }
    }
}