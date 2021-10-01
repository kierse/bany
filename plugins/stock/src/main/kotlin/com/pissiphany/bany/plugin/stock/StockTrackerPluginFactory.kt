package com.pissiphany.bany.plugin.stock

import com.pissiphany.bany.plugin.BanyConfigurablePlugin
import com.pissiphany.bany.plugin.BanyPlugin
import com.pissiphany.bany.plugin.BanyPluginFactory
import com.pissiphany.bany.plugin.stock.adapter.BigDecimalAdapter
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import org.pf4j.Extension

private const val STOCK_TRACKER = "stock-tracker"

@Extension
class EtradePluginFactory : BanyPluginFactory {
    override val pluginNames = setOf(STOCK_TRACKER)

    override fun createPlugin(
        pluginName: String,
        credentials: BanyPlugin.Credentials
    ): BanyConfigurablePlugin {
        val client = OkHttpClient.Builder().build()
        val moshi = Moshi.Builder()
            .add(BigDecimalAdapter)
            .build()

        return StockTrackerPlugin(credentials, client, moshi)
    }
}