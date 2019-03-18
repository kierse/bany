package com.pissiphany.bany.adapter.dataStructure

import java.time.OffsetDateTime

data class YnabTransaction(
    val id: String,
    val accountId: String,
    val amount Long,
    val date: OffsetDateTime
)
