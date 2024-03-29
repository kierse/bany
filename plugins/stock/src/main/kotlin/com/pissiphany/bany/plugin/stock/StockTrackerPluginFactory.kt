package com.pissiphany.bany.plugin.stock

import com.pissiphany.bany.plugin.BanyConfigurablePlugin
import com.pissiphany.bany.plugin.BanyPlugin
import com.pissiphany.bany.plugin.BanyPluginFactory
import com.pissiphany.bany.plugin.stock.adapter.BigDecimalAdapter
import com.pissiphany.bany.shared.logger
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient

private const val STOCK_TRACKER = "stock-tracker"

class StockTrackerPluginFactory : BanyPluginFactory {
    override val pluginNames = setOf(STOCK_TRACKER)
    private val logger by logger()

    override suspend fun createPlugin(
        pluginName: String,
        credentials: BanyPlugin.Credentials
    ): BanyConfigurablePlugin {
        val client = lazy { OkHttpClient.Builder().build() }
        val moshi = lazy {
            Moshi.Builder()
                .add(BigDecimalAdapter)
                .build()
        }

        logger.debug("Creating StockTrackerPlugin: $pluginName")
        return StockTrackerPlugin(pluginName, client, moshi, credentials)
    }
}