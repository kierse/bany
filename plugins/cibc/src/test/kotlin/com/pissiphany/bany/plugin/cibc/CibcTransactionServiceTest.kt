package com.pissiphany.bany.plugin.cibc

import com.pissiphany.bany.plugin.BanyPlugin
import com.pissiphany.bany.plugin.cibc.environment.CibcEnvironment
import com.pissiphany.bany.plugin.cibc.environment.Environment
import com.pissiphany.bany.plugin.cibc.environment.SimpliiEnvironment
import com.pissiphany.bany.plugin.cibc.mapper.CibcTransactionMapper
import com.pissiphany.bany.plugin.dataStructure.BanyPluginTransaction
import com.pissiphany.bany.plugin.dataStructure.YnabBudgetAccountIds
import com.squareup.moshi.Moshi
import okhttp3.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.File
import java.net.CookieManager
import java.net.CookiePolicy
import java.time.LocalDate

internal class CibcTransactionServiceTest {
    @Test
    fun setup__fetchAppConfig_fail() {
        val client = { request: Request ->
            Response.Builder()
                .request(request)
                .code(400) // anything but 200
                .protocol(Protocol.HTTP_2)
                .message("baz")
                .body(ResponseBody.create(MediaType.get("application/json"), "bar"))
                .build()
        }

        val service = CibcTransactionService(
            TestCredentials(connections = emptyList()),
            CibcEnvironment(),
            Moshi.Builder().build(),
            client,
            CibcTransactionMapper()
        )

        assertFalse(service.setup())
    }

    @Test
    fun setup__authenticate_fail() {
        val dummy = Request.Builder()
            .url("http://example.com")
            .build()
        val responses = listOf(
            Response.Builder()
                .request(dummy)
                .code(200)
                .protocol(Protocol.HTTP_2)
                .message("baz")
                .body(ResponseBody.create(MediaType.get("application/json"), "bar"))
                .build(),
            Response.Builder()
                .request(dummy)
                .code(200) // response code doesn't matter - looking for specific response header
                .protocol(Protocol.HTTP_2)
                .message("baz")
                .body(ResponseBody.create(MediaType.get("application/json"), "bar"))
                .build()
        )
        var cur = 0
        val client = { _: Request -> responses[cur++] }

        val service = CibcTransactionService(
            TestCredentials(connections = emptyList()),
            CibcEnvironment(),
            Moshi.Builder().build(),
            client,
            CibcTransactionMapper()
        )

        assertFalse(service.setup())
    }

    @Test
    fun setup__fetchAccounts_fail() {
        val dummy = Request.Builder()
            .url("http://example.com")
            .build()
        val responses = listOf(
            Response.Builder()
                .request(dummy)
                .code(200)
                .protocol(Protocol.HTTP_2)
                .message("baz")
                .body(ResponseBody.create(MediaType.get("application/json"), "bar"))
                .build(),
            Response.Builder()
                .request(dummy)
                .code(200)
                .addHeader(X_AUTH_TOKEN_HEADER, "token")
                .protocol(Protocol.HTTP_2)
                .message("baz")
                .body(ResponseBody.create(MediaType.get("application/json"), "bar"))
                .build(),
            Response.Builder()
                .request(dummy)
                .code(400)
                .protocol(Protocol.HTTP_2)
                .message("baz")
                .body(ResponseBody.create(MediaType.get("application/json"), "bar"))
                .build()
        )
        var cur = 0
        val client = { _: Request -> responses[cur++] }

        val service = CibcTransactionService(
            TestCredentials(connections = emptyList()),
            CibcEnvironment(),
            Moshi.Builder().build(),
            client,
            CibcTransactionMapper()
        )

        assertFalse(service.setup())
    }

    @Test
    fun setup__fetchAccounts_no_accounts() {
        val dummy = Request.Builder()
            .url("http://example.com")
            .build()
        val responses = listOf(
            Response.Builder()
                .request(dummy)
                .code(200)
                .protocol(Protocol.HTTP_2)
                .message("baz")
                .body(ResponseBody.create(MediaType.get("application/json"), "bar"))
                .build(),
            Response.Builder()
                .request(dummy)
                .code(200)
                .addHeader(X_AUTH_TOKEN_HEADER, "token")
                .protocol(Protocol.HTTP_2)
                .message("baz")
                .body(ResponseBody.create(MediaType.get("application/json"), "bar"))
                .build(),
            Response.Builder()
                .request(dummy)
                .code(200)
                .protocol(Protocol.HTTP_2)
                .message("baz")
                .body(ResponseBody.create(MediaType.get("application/json"), "{\"accounts\": []}"))
                .build()
        )
        var cur = 0
        val client = { _: Request -> responses[cur++] }

        val service = CibcTransactionService(
            TestCredentials(connections = emptyList()),
            CibcEnvironment(),
            Moshi.Builder().build(),
            client,
            CibcTransactionMapper()
        )

        assertFalse(service.setup())
    }

    @Test
    fun getYnabBudgetAccountIds() {
        val service = CibcTransactionService(
            TestCredentials(connections = listOf(TestConnection())),
            CibcEnvironment(),
            Moshi.Builder().build(),
            { fail() },
            CibcTransactionMapper()
        )

        val expected = listOf(YnabBudgetAccountIds(
            ynabBudgetId = "ynabBudgetId",
            ynabAccountId = "ynabAccountId"
        ))
        assertIterableEquals(expected, service.getYnabBudgetAccountIds())
    }

    @Test
    fun getNewBanyPluginTransactionsSince__unknown_account() {
        val json = """
        {
            "accounts": [
                {
                    "id": "id",
                    "number": "number",
                    "balance": 10,
                    "currency": "CAD"
                }
            ]
        }
        """.trimIndent()
        val dummy = Request.Builder()
            .url("http://example.com")
            .build()
        val responses = listOf(
            Response.Builder()
                .request(dummy)
                .code(200)
                .protocol(Protocol.HTTP_2)
                .message("baz")
                .body(ResponseBody.create(MediaType.get("application/json"), "bar"))
                .build(),
            Response.Builder()
                .request(dummy)
                .code(200)
                .addHeader(X_AUTH_TOKEN_HEADER, "token")
                .protocol(Protocol.HTTP_2)
                .message("baz")
                .body(ResponseBody.create(MediaType.get("application/json"), "bar"))
                .build(),
            Response.Builder()
                .request(dummy)
                .code(200)
                .protocol(Protocol.HTTP_2)
                .message("baz")
                .body(ResponseBody.create(MediaType.get("application/json"), json))
                .build()
        )
        var cur = 0
        val client = { _: Request -> responses[cur++] }

        val connection = TestConnection(
            ynabBudgetId = "ynabBudgetId",
            ynabAccountId = "ynabAccountId",
            thirdPartyAccountId = "unknown_account_id"
        )
        val ids = YnabBudgetAccountIds(
            ynabBudgetId = "ynabBudgetId",
            ynabAccountId = "ynabAccountId"
        )

        val service = CibcTransactionService(
            TestCredentials(connections = listOf(connection)),
            CibcEnvironment(),
            Moshi.Builder().build(),
            client,
            CibcTransactionMapper()
        )

        service.setup()

        assertThrows<IllegalArgumentException> {
            service.getNewBanyPluginTransactionsSince(ids, null)
        }
    }

    @Test
    fun getNewBanyPluginTransactionsSince__no_results() {
        val json = """
        {
            "accounts": [
                {
                    "id": "id",
                    "number": "number",
                    "balance": 10,
                    "currency": "CAD"
                }
            ]
        }
        """.trimIndent()
        val dummy = Request.Builder()
            .url("http://example.com")
            .build()
        val responses = listOf(
            Response.Builder()
                .request(dummy)
                .code(200)
                .protocol(Protocol.HTTP_2)
                .message("one")
                .body(ResponseBody.create(MediaType.get("application/json"), "bar"))
                .build(),
            Response.Builder()
                .request(dummy)
                .code(200)
                .addHeader(X_AUTH_TOKEN_HEADER, "token")
                .protocol(Protocol.HTTP_2)
                .message("two")
                .body(ResponseBody.create(MediaType.get("application/json"), "bar"))
                .build(),
            Response.Builder()
                .request(dummy)
                .code(200)
                .protocol(Protocol.HTTP_2)
                .message("three")
                .body(ResponseBody.create(MediaType.get("application/json"), json))
                .build(),
            Response.Builder()
                .request(dummy)
                .code(422)
                .protocol(Protocol.HTTP_2)
                .message("four")
                .body(ResponseBody.create(MediaType.get("application/json"), "{}"))
                .build()
        )
        var cur = 0
        val client = { _: Request -> responses[cur++] }

        val connection = TestConnection(
            ynabBudgetId = "ynabBudgetId",
            ynabAccountId = "ynabAccountId",
            thirdPartyAccountId = "number"
        )
        val ids = YnabBudgetAccountIds(
            ynabBudgetId = "ynabBudgetId",
            ynabAccountId = "ynabAccountId"
        )

        val service = CibcTransactionService(
            TestCredentials(connections = listOf(connection)),
            CibcEnvironment(),
            Moshi.Builder().build(),
            client,
            CibcTransactionMapper()
        )

        service.setup()

        assertTrue(service.getNewBanyPluginTransactionsSince(ids, null).isEmpty())
    }

    @Test
    fun getNewBanyPluginTransactionsSince__cibc() {
        assertTrue(getNewBanyPluginTransactionsSince(CibcEnvironment()).isNotEmpty())
    }

    @Test
    fun getNewBanyPluginTransactionsSince__simplii() {
        assertTrue(getNewBanyPluginTransactionsSince(SimpliiEnvironment()).isNotEmpty())
    }

    private fun getNewBanyPluginTransactionsSince(env: Environment): List<BanyPluginTransaction> {
        val dir = File(System.getProperty("user.home"), ".bany")
        val configFile = File(dir, "bany.config")
        if (!configFile.exists()) {
            println("unable to find config file, skipping!")
            return emptyList()
        }

        val moshi = Moshi.Builder().build()
        val configAdapter = moshi.adapter(TestConfig::class.java)
        val config = configAdapter.fromJson(configFile.readText())
            ?: throw Exception("unable to load config!")

        val ok = OkHttpClient
            .Builder()
            .cookieJar(QuotePreservingCookieJar(CookieManager(null, CookiePolicy.ACCEPT_ALL)))
            .build()
        val client = { request: Request -> ok.newCall(request).execute() }

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