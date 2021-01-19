package com.pissiphany.bany.plugin.dataStructure

import java.math.BigDecimal
import java.time.OffsetDateTime

sealed class BanyPluginTransaction {
    abstract val date: OffsetDateTime
    abstract val payee: String
    abstract val amount: BigDecimal
}

data class BanyPluginAccountTransaction(
    override val date: OffsetDateTime,
    override val payee: String,
    val memo: String,
    override val amount: BigDecimal
): BanyPluginTransaction()

data class BanyPluginAccountBalance(
    override val date: OffsetDateTime,
    override val payee: String,
    override val amount: BigDecimal
): BanyPluginTransaction()
