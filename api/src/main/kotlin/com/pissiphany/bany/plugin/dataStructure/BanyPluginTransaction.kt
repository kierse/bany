package com.pissiphany.bany.plugin.dataStructure

import java.time.OffsetDateTime

data class BanyPluginTransaction(
    val id: String,
    val date: OffsetDateTime,
    val amount: Long
)