package com.pissiphany.bany.plugin.equitable

import com.pissiphany.bany.plugin.dataStructure.BanyPluginBudgetAccountIds
import com.squareup.moshi.Moshi
import org.junit.jupiter.api.Test
import java.io.File

private val CONFIG_DIR = File(System.getProperty("user.home"), ".bany")
private val CONFIG_FILE = File(CONFIG_DIR, "equitable-life-integration.config")

class EquitableLifePluginIntegrationTest {
    private val credentials: EquitableLifePluginTest.Credentials? =
        CONFIG_FILE
            .takeIf(File::isFile)
            ?.let { file ->
                val moshi = Moshi.Builder().build()
                val adapter = moshi.adapter(EquitableLifePluginTest.Credentials::class.java)
                adapter.fromJson(file.readText())
            }

    @Test
    fun integration() {
        checkNotNull(credentials) { "Unable to initialize config! Does file exist at $CONFIG_FILE?" }

        EquitableLifePlugin(credentials)
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
}