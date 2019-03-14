package com.pissiphany.bany.dataStructure

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RetrofitAccount(
    val id: String, val name: String, val closed: Boolean, val balance: Long, val type: String
)
