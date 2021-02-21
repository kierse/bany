package com.pissiphany.bany.plugin.bitcoin

import com.pissiphany.bany.plugin.bitcoin.adapter.BigDecimalAdapter
import com.pissiphany.bany.plugin.dataStructure.BanyPluginBudgetAccountIds
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import org.junit.jupiter.api.Test
import java.io.File

private val CONFIG_DIR = File(System.getProperty("user.home"), ".bany")
private val CONFIG_FILE = File(CONFIG_DIR, "bitcoin-tracker-integration.config")

class BitcoinPluginIntegrationTest {
    private val plugin: BitcoinPlugin
    private val connections: List<BitcoinPluginTest.Connection>

    init {
        val file = checkNotNull(CONFIG_FILE.takeIf(File::isFile)) {
            "Unable to initialize config! $CONFIG_FILE does not exist / is not a file"
        }

        val client = OkHttpClient
            .Builder()
            .build()

        val moshi = Moshi.Builder()
            .add(BigDecimalAdapter())
            .build()

        val adapter = moshi.adapter(BitcoinPluginTest.Credentials::class.java)
        val credentials = checkNotNull(adapter.fromJson(file.readText()))

        connections = credentials.connections
        plugin = BitcoinPlugin(credentials, client, moshi)
    }

    @Test
    fun integration() {
        plugin.getNewBanyPluginTransactionsSince(BanyPluginBudgetAccountIds("budgetId", "accountId1"), null)
            .let { transactions ->
                println()
                println("Account 01: bitcoin")
                println(transactions.first())
                println()
            }
        plugin.getNewBanyPluginTransactionsSince(BanyPluginBudgetAccountIds("budgetId", "accountId2"), null)
            .let { transactions ->
                println()
                println("Account 02: bitcoin-cash")
                println(transactions.first())
                println()
            }
    }
}