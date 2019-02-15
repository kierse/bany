package com.pissiphany.bany.adapter.dataStructure

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.LocalDateTime

@JsonClass(generateAdapter = true)
data class YnabBudget(val id: String, val name: String, @field:Json(name = "last_modified_on") val lastModifiedOn: LocalDateTime)
