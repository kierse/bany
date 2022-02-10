package com.pissiphany.bany.plugin.stock

import com.pissiphany.bany.plugin.dataStructure.BanyPluginBudgetAccountIds
import com.pissiphany.bany.plugin.stock.adapter.BigDecimalAdapter
import com.squareup.moshi.Moshi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import org.junit.jupiter.api.Test
import java.io.File

private val CONFIG_DIR = File(System.getProperty("user.home"), ".bany")
private val CONFIG_FILE = File(CONFIG_DIR, "stock-tracker-integration.config")

@OptIn(ExperimentalCoroutinesApi::class)
class StockTrackerPluginIntegrationTest {
    private val credentials: StockTrackerPluginTest.Credentials

    private val client = lazy {
        OkHttpClient
            .Builder()
            .build()
    }

    private val moshi = lazy {
        Moshi.Builder()
            .add(BigDecimalAdapter)
            .build()
    }

    init {
        val file = checkNotNull(CONFIG_FILE.takeIf(File::isFile)) {
            "Unable to initialize config! $CONFIG_FILE does not exist / is not a file"
        }

        val adapter = moshi.value.adapter(StockTrackerPluginTest.Credentials::class.java)
        credentials = checkNotNull(adapter.fromJson(file.readText()))
    }

    @Test
    fun integration() = runTest {
        StockTrackerPlugin(client, moshi, credentials).run {
            setup()
            getNewBanyPluginTransactionsSince(BanyPluginBudgetAccountIds("budgetId", "accountId"), null)
                .let { transactions ->
                    println()
                    println("Account 01: crm")
                    println(transactions.first())
                    println()
                }
        }
    }
}