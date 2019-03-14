package com.pissiphany.bany.domain.dataStructure

data class UpdatedTransactions(val transactions: List<Transaction> = emptyList(), val lastKnowledgeOfServer: Int)