package com.pissiphany.bany.config

import com.pissiphany.bany.plugin.BanyPlugin
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class BanyConfigConnection(
    val name: String,
    @Json(name = "ynab_budget_id") override val ynabBudgetId: String,
    @Json(name = "ynab_account_id") override val ynabAccountId: String,
    @Json(name = "third_party_account_id") override val thirdPartyAccountId: String
) : BanyPlugin.Connection