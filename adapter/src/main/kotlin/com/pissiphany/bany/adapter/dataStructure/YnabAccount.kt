package com.pissiphany.bany.adapter.dataStructure

data class YnabAccount(
    val id: String,
    val name: String,
    val closed: Boolean,
    val balanceInMilliUnits: Int,
    val type: String
)
