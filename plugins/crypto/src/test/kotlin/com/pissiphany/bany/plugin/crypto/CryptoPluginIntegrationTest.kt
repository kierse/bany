package com.pissiphany.bany.plugin.crypto

import com.pissiphany.bany.plugin.BanyPlugin
import com.pissiphany.bany.plugin.crypto.adapter.BigDecimalAdapter
import com.pissiphany.bany.plugin.dataStructure.BanyPluginBudgetAccountIds
import com.squareup.moshi.Moshi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import org.junit.jupiter.api.Test
import java.io.File

private val CONFIG_DIR = File(System.getProperty("user.home"), ".bany")
private val CONFIG_FILE = File(CONFIG_DIR, "crypto-tracker-integration.config")

@OptIn(ExperimentalCoroutinesApi::class)
class CryptoPluginIntegrationTest {
    private val credentials: BanyPlugin.Credentials

    private val client = lazy {
        OkHttpClient
            .Builder()
            .build()
    }

    private val moshi = lazy {
        Moshi.Builder()
            .add(BigDecimalAdapter())
            .build()
    }

    init {
        val file = checkNotNull(CONFIG_FILE.takeIf(File::isFile)) {
            "Unable to initialize config! $CONFIG_FILE does not exist / is not a file"
        }

        val adapter = moshi.value.adapter(CryptoPluginTest.Credentials::class.java)
        credentials = checkNotNull(adapter.fromJson(file.readText()))
    }

    @Test
    fun integration() = runTest {
        with(CryptoPlugin("crypto", client, moshi, credentials)) {
            setup()
            getNewBanyPluginTransactionsSince(BanyPluginBudgetAccountIds("budgetId", "bitcoin"), null)
                .let { transactions ->
                    println()
                    println("Account 01: bitcoin")
                    println(transactions.first())
                    println()
                }
            getNewBanyPluginTransactionsSince(BanyPluginBudgetAccountIds("budgetId", "bitcoin-cash"), null)
                .let { transactions ->
                    println()
                    println("Account 02: bitcoin-cash")
                    println(transactions.first())
                    println()
                }
            getNewBanyPluginTransactionsSince(BanyPluginBudgetAccountIds("budgetId", "ethereum"), null)
                .let { transactions ->
                    println()
                    println("Account 03: ethereum")
                    println(transactions.first())
                    println()
                }
        }
    }
}