package com.pissiphany.bany.adapter.dataStructure

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class YnabTransactions(val transactions: List<YnabTransaction>)