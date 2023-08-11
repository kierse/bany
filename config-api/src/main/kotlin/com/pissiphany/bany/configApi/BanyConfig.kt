package com.pissiphany.bany.configApi

import com.pissiphany.bany.plugin.PluginName
import com.squareup.moshi.JsonClass

typealias YnabBudgetId = String

@JsonClass(generateAdapter = true)
class BanyConfig(val ynabApiToken: String, val plugins: Map<PluginName, List<ServiceCredentials>>)

@JsonClass(generateAdapter = true)
class ServiceCredentials(
    val username: String = "",
    val password: String = "",
    val connections: Map<YnabBudgetId, List<ServiceConnection>>,
    val enabled: Boolean = true,
    val description: String = "",
    val data: Map<String, String> = emptyMap()
)

@JsonClass(generateAdapter = true)
class ServiceConnection(
    val name: String,
    val ynabAccountId: String,
    val thirdPartyAccountId: String = "",
    val enabled: Boolean = true,
    val data: Map<String, String> = emptyMap(),
)
