package com.pissiphany.bany.mapper

import com.pissiphany.bany.adapter.dataStructure.YnabAccountBalance
import com.pissiphany.bany.adapter.dataStructure.YnabAccountTransaction
import com.pissiphany.bany.plugin.dataStructure.BanyPluginAccountBalance
import com.pissiphany.bany.plugin.dataStructure.BanyPluginAccountTransaction
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.math.BigDecimal
import java.time.OffsetDateTime

internal class BanyPluginDataMapperTest {
    @Test
    fun `toYnabTransaction - account transaction`() {
        val expected = YnabAccountTransaction(null, "accountId", OffsetDateTime.now(), "payee", "memo", -10000)
        val pluginTransaction = BanyPluginAccountTransaction(
            date = expected.date,
            payee = expected.payee,
            memo = expected.memo,
            amount = BigDecimal("10.00")
        )

        assertEquals(expected, BanyPluginDataMapper().toYnabTransaction(pluginTransaction, "accountId"))
    }

    @Test
    fun `toYnabTransaction - account balance`() {
        val expected = YnabAccountBalance("accountId", OffsetDateTime.now(), "payee", 1500)
        val pluginTransaction = BanyPluginAccountBalance(
            date = expected.date,
            payee = expected.payee,
            amount = BigDecimal("10.00")
        )

        assertEquals(expected, BanyPluginDataMapper().toYnabTransaction(pluginTransaction, "accountId"))
    }
}