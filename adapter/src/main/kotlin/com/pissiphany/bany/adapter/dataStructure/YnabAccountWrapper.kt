package com.pissiphany.bany.adapter.dataStructure

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class YnabAccountWrapper(@Json(name = "account") val ynabAccount: YnabAccount)