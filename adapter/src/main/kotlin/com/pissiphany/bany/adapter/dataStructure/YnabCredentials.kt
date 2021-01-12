package com.pissiphany.bany.adapter.dataStructure

import com.pissiphany.bany.plugin.BanyPlugin

class YnabCredentials(
    override val username: String,
    override val password: String,
    override val connections: List<YnabConnection>,
    override val data: Map<String, String>,
    val enabled: Boolean = true,
    val description: String = ""
) : BanyPlugin.Credentials