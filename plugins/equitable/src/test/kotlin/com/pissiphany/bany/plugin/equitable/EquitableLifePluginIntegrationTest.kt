package com.pissiphany.bany.plugin.equitable

import com.pissiphany.bany.plugin.dataStructure.BanyPluginBudgetAccountIds
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import org.junit.jupiter.api.Test
import java.io.File

private val CONFIG_DIR = File(System.getProperty("user.home"), ".bany")
private val CONFIG_FILE = File(CONFIG_DIR, "equitable-life-instrumentation.config")

class EquitableLifePluginIntegrationTest {
    private val config: EquitableLifePluginConfig?

    init {
        config = if (CONFIG_FILE.isFile) {
            val moshi = Moshi.Builder().build()
            val adapter = moshi.adapter(EquitableLifePluginConfig::class.java)
            adapter.fromJson(CONFIG_FILE.readText())
        } else {
            null
        }
    }

    @Test
    fun integration() {
        if (config == null || !config.enabled) {
            println()
            println("##########################")
            println("Skipping integration test!")
            println("##########################")
            println()
            return
        }

        EquitableLifePlugin(config.credentials)
            .apply {
                check(setup()) { "Unable to configure plugin!" }

                val transactions = getNewBanyPluginTransactionsSince(
                    BanyPluginBudgetAccountIds("budgetId", "accountId"), null
                )

                println()
                println("Account balance:")
                println(transactions.first())
                println()

                tearDown()
            }
    }

    @JsonClass(generateAdapter = true)
    class EquitableLifePluginConfig(
        val credentials: EquitableLifePluginTest.Credentials,
        val enabled: Boolean
    )
}