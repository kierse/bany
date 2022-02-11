package com.pissiphany.bany.plugin.equitable

import com.pissiphany.bany.plugin.BanyConfigurablePlugin
import com.pissiphany.bany.plugin.BanyPlugin
import com.pissiphany.bany.plugin.BanyPluginFactory
import com.pissiphany.bany.plugin.equitable.client.EquitableClientImpl
import org.pf4j.Extension

internal const val EQUITABLE_LIFE = "equitable-life"

@Extension
class EquitableLifePluginFactory : BanyPluginFactory {
    override val pluginNames = setOf(EQUITABLE_LIFE)

    override suspend fun createPlugin(pluginName: String, credentials: BanyPlugin.Credentials): BanyConfigurablePlugin {
        return EquitableLifePlugin(EquitableClientImpl(), credentials)
    }
}