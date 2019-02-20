package com.pissiphany.bany.adapter.dataStructure

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class YnabTransactionsWrapper(
    @Json(name = "transactions") val ynabTransactions: List<YnabTransaction>,
    @Json(name = "server_knowledge") val serverKnowledge: Int
)