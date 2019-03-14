package com.pissiphany.bany.dataStructure

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RetrofitAccounts(val accounts: List<RetrofitAccount>)