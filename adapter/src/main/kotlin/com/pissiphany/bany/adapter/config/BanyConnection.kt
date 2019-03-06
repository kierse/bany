package com.pissiphany.bany.adapter.config

import com.pissiphany.bany.adapter.plugin.BanyPlugin
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class BanyConnection(
    val name: String,
    @Json(name = "ynab_budget_id") override val ynabBudgetId: String,
    @Json(name = "ynab_account_id") override val ynabAccountId: String,
    @Json(name = "third_party_account_id") override val thirdPartyAccountId: String
) : BanyPlugin.Connection