package com.pissiphany.bany.adapter.dataStructure

import java.time.OffsetDateTime

data class YnabTransaction(
    val id: String?,
    val accountId: String,
    val date: OffsetDateTime,
    val payee: String,
    val memo: String,
    val amountInMilliUnits: Int
)
