package com.pissiphany.bany.adapter.dataStructure

import java.time.OffsetDateTime

sealed class YnabTransaction {
    abstract val accountId: String // TODO do I really need this??
    abstract val date: OffsetDateTime
    abstract val payee: String
    abstract val amountInMilliUnits: Int
}

data class YnabAccountTransaction(
    val id: String? = null,
    override val accountId: String,
    override val date: OffsetDateTime,
    override val payee: String,
    val memo: String,
    override val amountInMilliUnits: Int
): YnabTransaction()

data class YnabAccountBalance(
    override val accountId: String,
    override val date: OffsetDateTime,
    override val payee: String,
    override val amountInMilliUnits: Int
): YnabTransaction()
