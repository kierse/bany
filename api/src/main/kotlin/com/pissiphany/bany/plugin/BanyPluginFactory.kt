package com.pissiphany.bany.plugin

import org.pf4j.ExtensionPoint

typealias PluginName = String

interface BanyPluginFactory : ExtensionPoint {
    val pluginNames: Set<PluginName>
    fun createPlugin(
        pluginName: String,
        credentials: BanyPlugin.Credentials
    ): BanyConfigurablePlugin
}