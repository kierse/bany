package com.pissiphany.bany.adapter.dataStructure

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class YnabAccount(val id: String, val name: String, val closed: Boolean, val balance: Long, val type: String)
