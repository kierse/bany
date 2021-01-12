package com.pissiphany.bany.plugin.dataStructure

import java.math.BigDecimal
import java.time.OffsetDateTime

sealed class BanyPluginTransaction {
    abstract val date: OffsetDateTime
    abstract val payee: String
    abstract val memo: String
}

data class DebitTransaction(
    override val date: OffsetDateTime,
    override val payee: String,
    override val memo: String,
    val debit: BigDecimal
): BanyPluginTransaction()

data class CreditTransaction(
    override val date: OffsetDateTime,
    override val payee: String,
    override val memo: String,
    val credit: BigDecimal
): BanyPluginTransaction()

data class NewBalanceTransaction(
    override val date: OffsetDateTime,
    override val payee: String,
    override val memo: String,
    val balance: BigDecimal
): BanyPluginTransaction()
