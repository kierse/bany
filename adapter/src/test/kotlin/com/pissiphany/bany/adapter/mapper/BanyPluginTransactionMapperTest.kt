package com.pissiphany.bany.adapter.mapper

import com.pissiphany.bany.plugin.dataStructure.BanyPluginTransaction
import com.pissiphany.bany.domain.dataStructure.Transaction
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.time.LocalDate

internal class BanyPluginTransactionMapperTest {
    @Test
    fun toTransaction() {
        val transaction = Transaction("id", LocalDate.now(), 10L)
        val pluginTransaction = BanyPluginTransaction(transaction.id, transaction.date, transaction.amount)

        assertEquals(transaction, BanyPluginTransactionMapper().toTransaction(pluginTransaction))
    }
}