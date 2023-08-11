package com.pissiphany.bany.plugin.sample

import com.pissiphany.bany.plugin.BanyConfigurablePlugin
import com.pissiphany.bany.plugin.BanyPlugin
import com.pissiphany.bany.plugin.BanyPluginFactory

class SamplePluginFactory : BanyPluginFactory {
    override val pluginNames = setOf("sample")
    override suspend fun createPlugin(pluginName: String, credentials: BanyPlugin.Credentials): BanyConfigurablePlugin = SamplePlugin(pluginName)
}