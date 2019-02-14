package com.pissiphany.bany.adapter.dataStructure

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class YnabAccounts(val accounts: List<YnabAccount>)