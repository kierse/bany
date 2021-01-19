package com.pissiphany.bany.domain.dataStructure

import java.time.OffsetDateTime

data class SyncTransactionsResult(
    val budgetAccountIds: BudgetAccountIds,
    val dateOfLastTransaction: OffsetDateTime?,
    val transactions: List<Transaction>
)