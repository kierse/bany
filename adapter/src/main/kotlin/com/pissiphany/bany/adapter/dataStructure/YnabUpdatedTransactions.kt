package com.pissiphany.bany.adapter.dataStructure

data class YnabUpdatedTransactions(val transactions: List<YnabAccountTransaction> = emptyList(), val lastKnowledgeOfServer: Int)