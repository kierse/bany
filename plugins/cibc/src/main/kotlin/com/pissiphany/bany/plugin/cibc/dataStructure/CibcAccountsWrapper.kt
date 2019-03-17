package com.pissiphany.bany.plugin.cibc.dataStructure

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CibcAccountsWrapper(val accounts: List<CibcAccount>) {
    @JsonClass(generateAdapter = true)
    data class CibcAccount(val id: String, val number: String, val balance: String, val currency: String)
}
