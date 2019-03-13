package com.pissiphany.bany.config

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class BanyConfig(@Json(name = "ynab_api_token") val ynabApiToken: String, val plugins: Map<String, BanyPluginConfiguration>)
