package com.pissiphany.bany.adapter.dataStructure

import com.squareup.moshi.JsonClass
import java.time.LocalTime

@JsonClass(generateAdapter = true)
data class YnabTransaction(
    val id: String, val accountId: String, val amount: Long, val date: LocalTime
)
