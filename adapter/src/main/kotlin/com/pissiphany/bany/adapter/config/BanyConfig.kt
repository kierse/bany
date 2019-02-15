package com.pissiphany.bany.adapter.config

import com.squareup.moshi.Json

class BanyConfig(@field:Json(name = "ynab_api_token") val ynabApiToken: String, val plugins: List<Plugin>)

class Plugin(val type: String, val username: String, val password: String, val connections: List<Connection>)

class Connection(
    val name: String,
    @field:Json(name = "ynab_budget_id") val ynabBudgetId: String,
    @field:Json(name = "ynab_account_id") val ynabAccountId: String,
    @field:Json(name = "third_party_account_id") val thirdPartyAccountId: String
)