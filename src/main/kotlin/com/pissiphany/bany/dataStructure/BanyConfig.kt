package com.pissiphany.bany.dataStructure

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class BanyConfig(val ynabApiToken: String, val plugins: Map<String, List<ServiceCredentials>>)

@JsonClass(generateAdapter = true)
class ServiceCredentials(
    val username: String,
    val password: String,
    val connections: Map<String, List<ServiceConnection>>,
    val enabled: Boolean = true,
    val description: String = "",
    val data: Map<String, String> = emptyMap()
)

@JsonClass(generateAdapter = true)
class ServiceConnection(
    val name: String,
    val ynabAccountId: String,
    val thirdPartyAccountId: String,
    val enabled: Boolean = true,
    val data: Map<String, String> = emptyMap(),
)
