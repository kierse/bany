package com.pissiphany.bany.adapter.dataStructure

data class ViewModel(
    val records: List<Record>
) {
    data class Record(
        val syncIndex: String,
        val budgetId: String,
        val budgetName: String,
        val accountId: String,
        val accountName: String,
        val latestTransactionDate: String = "",
        val numberOfTransactionsFound: String,
        val transactions: List<String>
    )
}