package com.pissiphany.bany.dataStructure

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RetrofitTransactionsWrapper(
    val transactions: List<RetrofitTransaction>, val server_knowledge: Int
)