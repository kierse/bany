package com.pissiphany.bany.plugin.equitable

import com.pissiphany.bany.plugin.dataStructure.BanyPluginBudgetAccountIds
import com.pissiphany.bany.plugin.equitable.client.EquitableClientImpl
import com.pissiphany.bany.plugin.equitable.client.OkHttpWrapper
import com.pissiphany.bany.plugin.equitable.client.OkHttpWrapperImpl
import com.squareup.moshi.Moshi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import org.jsoup.Jsoup
import org.junit.jupiter.api.Test
import java.io.File
import java.io.InputStream
import java.nio.charset.Charset

private val CONFIG_DIR = File(System.getProperty("user.home"), ".bany")
private val CONFIG_FILE = File(CONFIG_DIR, "equitable-life-integration.config")

@OptIn(ExperimentalCoroutinesApi::class)
class EquitableLifePluginIntegrationTest {
    private val credentials: EquitableLifePluginTest.Credentials? =
        CONFIG_FILE
            .takeIf(File::isFile)
            ?.let { file ->
                val moshi = Moshi.Builder().build()
                val adapter = moshi.adapter(EquitableLifePluginTest.Credentials::class.java)
                adapter.fromJson(file.readText())
            }

    private val client = lazy {
        OkHttpClient
            .Builder()
            .followRedirects(false)
            .build()
    }

    @Test
    fun integration() = runTest {
        checkNotNull(credentials) { "Unable to initialize config! Does file exist at $CONFIG_FILE?" }

        val root = EQUITABLE_ROOT.toHttpUrl()
        val processor = { bodyStream: InputStream, charset: Charset ->
            Jsoup.parse(bodyStream, charset.name(), root.toString())
        }
        val clientWrapper: OkHttpWrapper = OkHttpWrapperImpl(client, processor)

        EquitableLifePlugin(EquitableClientImpl(clientWrapper, root), credentials)
            .apply {
                check(setup()) { "Unable to configure plugin!" }

                getNewBanyPluginTransactionsSince(BanyPluginBudgetAccountIds("budgetId", "accountId1"), null)
                    .let { transactions ->
                        println()
                        println("Account 01: insurance")
                        println(transactions.first())
                        println()
                    }
                getNewBanyPluginTransactionsSince(BanyPluginBudgetAccountIds("budgetId", "accountId2"), null)
                    .let { transactions ->
                        println()
                        println("Account 02: insurance")
                        println(transactions.first())
                        println()
                    }
                getNewBanyPluginTransactionsSince(BanyPluginBudgetAccountIds("budgetId", "accountId3"), null)
                    .let { transactions ->
                        println()
                        println("Account 03: liability")
                        println(transactions.first())
                        println()
                    }
                getNewBanyPluginTransactionsSince(BanyPluginBudgetAccountIds("budgetId", "accountId4"), null)
                    .let { transactions ->
                        println()
                        println("Account 04: investment")
                        println(transactions.first())
                        println()
                    }

                tearDown()
            }
    }
}