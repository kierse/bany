package com.pissiphany.bany.domain.dataStructure

import java.time.LocalDate

data class SyncTransactionsResult(
    val budget: Budget, val account: Account, val dateOfLastTransaction: LocalDate?, val transactions: List<Transaction>
)