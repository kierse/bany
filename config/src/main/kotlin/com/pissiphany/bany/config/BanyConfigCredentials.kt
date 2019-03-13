package com.pissiphany.bany.config

import com.pissiphany.bany.plugin.BanyPlugin
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class BanyConfigCredentials(
    override val username: String,
    override val password: String,
    override val connections: List<BanyConfigConnection>,
    val enabled: Boolean = true,
    val description: String = ""
) : BanyPlugin.Credentials