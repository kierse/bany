package com.pissiphany.bany.domain.dataStructure

data class UpdatedTransactions(val transactions: List<Transaction>, val lastKnowledgeOfServer: Int)