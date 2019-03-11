package com.pissiphany.bany.adapter.config

import com.pissiphany.bany.plugin.BanyPlugin
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class BanyPluginConfiguration(
    override val username: String,
    override val password: String,
    override val connections: List<BanyConnection>,
    val enabled: Boolean = true
) : BanyPlugin.Configuration