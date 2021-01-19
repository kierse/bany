package com.pissiphany.bany.domain.dataStructure

import java.time.OffsetDateTime

sealed class Transaction {
    abstract val date: OffsetDateTime
    abstract val payee: String
    abstract val amountInCents: Int
}

data class AccountTransaction(
    val id: String?,
    override val date: OffsetDateTime,
    override val payee: String,
    val memo: String,
    override val amountInCents: Int
): Transaction()

data class AccountBalance(
    override val date: OffsetDateTime,
    override val payee: String,
    override val amountInCents: Int
): Transaction()
