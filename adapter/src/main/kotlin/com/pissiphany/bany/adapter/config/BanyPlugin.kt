package com.pissiphany.bany.adapter.config

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class BanyPlugin(val username: String, val password: String, val connections: List<BanyConnection>, val enabled: Boolean = true)