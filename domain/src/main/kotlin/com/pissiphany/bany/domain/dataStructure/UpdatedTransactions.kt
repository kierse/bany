package com.pissiphany.bany.domain.dataStructure

data class UpdatedTransactions(val transactions: List<AccountTransaction> = emptyList(), val lastKnowledgeOfServer: Int)