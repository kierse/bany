package com.pissiphany.bany

import com.pissiphany.bany.plugin.BanyPluginFactory
import com.pissiphany.bany.plugin.crypto.CryptoPluginFactory
import com.pissiphany.bany.plugin.equitable.EquitableLifePluginFactory
import com.pissiphany.bany.plugin.stock.StockTrackerPluginFactory

object PluginFactoryManager {
    val plugins: List<BanyPluginFactory> = listOf(
        CryptoPluginFactory(),
        EquitableLifePluginFactory(),
        StockTrackerPluginFactory()
    )
}