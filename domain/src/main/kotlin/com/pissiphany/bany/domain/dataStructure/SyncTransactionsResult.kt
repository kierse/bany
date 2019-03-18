package com.pissiphany.bany.domain.dataStructure

import java.time.OffsetDateTime

data class SyncTransactionsResult(
    val budget: Budget,
    val account: Account,
    val dateOfLastTransaction: OffsetDateTime?,
    val transactions: List<Transaction>
)