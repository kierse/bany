package com.pissiphany.bany.dataStructure

import com.squareup.moshi.JsonClass
import java.time.OffsetDateTime

@JsonClass(generateAdapter = true)
data class RetrofitBudget(
    val id: String, val name: String, val last_modified_on: OffsetDateTime
)
