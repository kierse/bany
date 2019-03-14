package com.pissiphany.bany.adapter.dataStructure

data class YnabUpdatedTransactions(val transactions: List<YnabTransaction> = emptyList(), val lastKnowledgeOfServer: Int)