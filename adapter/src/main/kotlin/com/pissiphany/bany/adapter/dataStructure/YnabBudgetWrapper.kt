package com.pissiphany.bany.adapter.dataStructure

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class YnabBudgetWrapper(
    @Json(name = "budget") val ynabBudget: YnabBudget, @Json(name = "server_knowledge") val serverKnowledge: Int
)