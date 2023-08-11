package com.pissiphany.bany.plugin

typealias PluginName = String

interface BanyPluginFactory {
    val pluginNames: Set<PluginName>

    suspend fun createPlugin(
        pluginName: String,
        credentials: BanyPlugin.Credentials
    ): BanyConfigurablePlugin
}