package com.pissiphany.bany.plugin

import org.pf4j.ExtensionPoint

interface BanyPluginFactory : ExtensionPoint {
    val pluginNames: Set<String>
    fun createPlugin(pluginName: String, credentials: BanyPlugin.Credentials): BanyPlugin
}