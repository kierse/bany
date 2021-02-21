package com.pissiphany.bany.plugin.bitcoin

import com.pissiphany.bany.plugin.BanyConfigurablePlugin
import com.pissiphany.bany.plugin.BanyPlugin
import com.pissiphany.bany.plugin.BanyPluginFactory
import com.pissiphany.bany.plugin.bitcoin.adapter.BigDecimalAdapter
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import org.pf4j.Extension

internal const val BITCOIN_TRACKER = "bitcoin-tracker"

@Extension
class BitcoinPluginFactory : BanyPluginFactory {
    override val pluginNames = setOf(BITCOIN_TRACKER)

    private val client = OkHttpClient
        .Builder()
        .build()

    private val moshi = Moshi.Builder()
        .add(BigDecimalAdapter())
        .build()

    override fun createPlugin(pluginName: String, credentials: BanyPlugin.Credentials): BanyConfigurablePlugin {
        return BitcoinPlugin(credentials, client, moshi)
    }
}