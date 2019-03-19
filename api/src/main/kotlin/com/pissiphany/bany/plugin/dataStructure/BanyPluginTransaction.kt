package com.pissiphany.bany.plugin.dataStructure

import java.math.BigDecimal
import java.time.OffsetDateTime

data class BanyPluginTransaction(
    val date: OffsetDateTime,
    val payee: String,
    val memo: String,
    val debit: BigDecimal,
    val credit: BigDecimal
)