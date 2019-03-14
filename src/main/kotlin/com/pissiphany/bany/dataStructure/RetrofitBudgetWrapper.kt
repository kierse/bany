package com.pissiphany.bany.dataStructure

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RetrofitBudgetWrapper(val budget: RetrofitBudget, val server_knowledge: Int)
