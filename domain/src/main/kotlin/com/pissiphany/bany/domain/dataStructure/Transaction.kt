package com.pissiphany.bany.domain.dataStructure

import java.time.OffsetDateTime

data class Transaction(
    val id: String?,
    val date: OffsetDateTime,
    val payee: String,
    val memo: String,
    val amountInCents: Int
)