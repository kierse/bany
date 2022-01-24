package com.pissiphany.bany.plugin.crypto

import com.pissiphany.bany.plugin.BanyConfigurablePlugin
import com.pissiphany.bany.plugin.BanyPlugin
import com.pissiphany.bany.plugin.BanyPluginFactory
import com.pissiphany.bany.plugin.crypto.adapter.BigDecimalAdapter
import com.pissiphany.bany.shared.logger
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import org.pf4j.Extension

internal const val CRYPTO_TRACKER = "crypto-tracker"

@Extension
class CryptoPluginFactory : BanyPluginFactory {
    override val pluginNames = setOf(CRYPTO_TRACKER)
    private val logger by logger()

    private val client = OkHttpClient
        .Builder()
        .build()

    private val moshi = Moshi.Builder()
        .add(BigDecimalAdapter())
        .build()

    override fun createPlugin(pluginName: String, credentials: BanyPlugin.Credentials): BanyConfigurablePlugin {
        logger.debug("Creating CryptoPlugin")
        return CryptoPlugin(credentials, client, moshi)
    }
}