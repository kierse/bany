package com.pissiphany.bany.plugin.cibc

import com.pissiphany.bany.plugin.BanyPlugin
import com.pissiphany.bany.plugin.cibc.adapter.CibcAccountsWrapperAdapter
import com.pissiphany.bany.plugin.cibc.environment.CibcEnvironment
import com.pissiphany.bany.plugin.cibc.environment.Environment
import com.pissiphany.bany.plugin.cibc.environment.SimpliiEnvironment
import com.pissiphany.bany.plugin.cibc.mapper.CibcTransactionMapper
import com.pissiphany.bany.plugin.dataStructure.BanyPluginTransaction
import com.pissiphany.bany.plugin.dataStructure.YnabBudgetAccountIds
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File
import java.net.CookieManager
import java.net.CookiePolicy
import java.time.LocalDate

internal class CibcTransactionServiceTest {
    @Test
    fun getYnabBudgetAccountIds() {
        val service = CibcTransactionService(
            TestCredentials(connections = listOf(TestConnection())),
            CibcEnvironment(),
            Moshi.Builder().build(),
            OkHttpClient(),
            CibcTransactionMapper()
        )

        val expected = listOf(YnabBudgetAccountIds(
            ynabBudgetId = "ynabBudgetId",
            ynabAccountId = "ynabAccountId"
        ))
        assertIterableEquals(expected, service.getYnabBudgetAccountIds())
    }

    @Test
    fun getNewBanyPluginTransactionsSince__cibc() {
        val moshi = Moshi.Builder().build()
        assertTrue(getNewBanyPluginTransactionsSince(CibcEnvironment(), moshi).isNotEmpty())
    }

    @Test
    fun getNewBanyPluginTransactionsSince__simplii() {
        val moshi = Moshi.Builder()
            .add(CibcAccountsWrapperAdapter())
            .build()
        assertTrue(getNewBanyPluginTransactionsSince(SimpliiEnvironment(), moshi).isNotEmpty())
    }

    private fun getNewBanyPluginTransactionsSince(
        env: Environment, moshi: Moshi
    ): List<BanyPluginTransaction> {
        val dir = File(System.getProperty("user.home"), ".bany")
        val configFile = File(dir, "bany.config")
        if (!configFile.exists()) {
            println("unable to find config file, skipping!")
            return emptyList()
        }

        val configAdapter = moshi.adapter(TestConfig::class.java)
        val config = configAdapter.fromJson(configFile.readText())
            ?: throw Exception("unable to load config!")

        val client = OkHttpClient
            .Builder()
            .cookieJar(QuotePreservingCookieJar(CookieManager(null, CookiePolicy.ACCEPT_ALL)))
            .build()

        val credentials = config.plugins[env.brand]?.first()
            ?: throw Exception("unable to find credentials!")

        // TODO do some validation to make sure values aren't empty!!
        val budgetAccountIds = YnabBudgetAccountIds(
            ynabBudgetId = credentials.connections[0].ynabBudgetId,
            ynabAccountId = credentials.connections[0].ynabAccountId
        )

        val service = CibcTransactionService(credentials, env, moshi, client, CibcTransactionMapper())
        try {
            if (!service.setup()) return fail("setup failed!")

            return service.getNewBanyPluginTransactionsSince(
                budgetAccountIds, LocalDate.now().minusMonths(1L)
            )
        } finally {
            service.tearDown()
        }
    }

    class TestConfig(val plugins: Map<String, List<TestCredentials>>)

    class TestCredentials(
        override val username: String = "username",
        override val password: String = "password",
        override val connections: List<TestConnection> = emptyList()
    ) : BanyPlugin.Credentials

    class TestConnection(
        override val ynabBudgetId: String = "ynabBudgetId",
        override val ynabAccountId: String = "ynabAccountId",
        override val thirdPartyAccountId: String = "thirdPartyAccountId"
    ) : BanyPlugin.Connection
}