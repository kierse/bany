package com.pissiphany.bany.adapter.dataStructure

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.LocalDate

@JsonClass(generateAdapter = true)
data class YnabTransaction(
    val id: String, @Json(name = "account_id") val accountId: String, val amount: Long, val date: LocalDate
)
