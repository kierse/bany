package com.pissiphany.bany.plugin.stock

import com.pissiphany.bany.plugin.BanyPlugin
import com.pissiphany.bany.plugin.BanyPluginFactory
import com.pissiphany.bany.plugin.SuspendableBanyConfigurablePlugin
import com.pissiphany.bany.plugin.stock.adapter.BigDecimalAdapter
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import org.pf4j.Extension

private const val STOCK_TRACKER = "stock-tracker"

@Extension
class StockTrackerPluginFactory : BanyPluginFactory {
    override val pluginNames = setOf(STOCK_TRACKER)

    override suspend fun createSuspendablePlugin(
        pluginName: String,
        credentials: BanyPlugin.Credentials
    ): SuspendableBanyConfigurablePlugin {
        val client = lazy { OkHttpClient.Builder().build() }
        val moshi = lazy {
            Moshi.Builder()
                .add(BigDecimalAdapter)
                .build()
        }

        return StockTrackerPlugin(client, moshi, credentials)
    }
}