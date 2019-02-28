package com.pissiphany.bany.adapter.config

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class BanyConnection(
    val name: String,
    @field:Json(name = "ynab_budget_id") val ynabBudgetId: String,
    @field:Json(name = "ynab_account_id") val ynabAccountId: String,
    @field:Json(name = "third_party_account_id") val thirdPartyAccountId: String
)