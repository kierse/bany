package com.pissiphany.bany.adapter.dataStructure

import com.squareup.moshi.JsonClass
import java.time.LocalDateTime

@JsonClass(generateAdapter = true)
data class YnabBudget(val id: String, val name: String, val last_modified_on: LocalDateTime)
