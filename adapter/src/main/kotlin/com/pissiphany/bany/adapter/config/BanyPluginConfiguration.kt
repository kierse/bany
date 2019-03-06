package com.pissiphany.bany.adapter.config

import com.pissiphany.bany.adapter.plugin.BanyPlugin
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class BanyPluginConfiguration(
    override val username: String,
    override val password: String,
    override val connections: List<BanyConnection>,
    override val enabled: Boolean = true
) : BanyPlugin.Configuration