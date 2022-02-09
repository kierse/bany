package com.pissiphany.bany.plugin.stock

import com.pissiphany.bany.plugin.BanyPlugin
import com.pissiphany.bany.plugin.dataStructure.BanyPluginBudgetAccountIds
import com.pissiphany.bany.plugin.stock.StockTrackerPlugin.Companion.COUNT
import com.pissiphany.bany.plugin.stock.StockTrackerPlugin.Companion.CURRENCY
import com.pissiphany.bany.plugin.stock.StockTrackerPlugin.Companion.CURRENCY_API_TOKEN
import com.pissiphany.bany.plugin.stock.StockTrackerPlugin.Companion.STOCK_API_TOKEN
import com.pissiphany.bany.plugin.stock.StockTrackerPlugin.Companion.TICKER
import com.pissiphany.bany.plugin.stock.adapter.BigDecimalAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.File
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

/**
 *  API query for test data:
 *
 */
private val RESOURCES_FILE = File("src/test/resources/json")

private const val TIMEOUT = 0L

@OptIn(ExperimentalCoroutinesApi::class)
class StockTrackerPluginTest {
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

    private val validConnection = Connection(
        name = "name1",
        ynabBudgetId = "budget1",
        ynabAccountId = "account2",
        data = mutableMapOf(
            COUNT to "10",
            CURRENCY to "cad",
            TICKER to "crm"
        )
    )

    private var credentials = Credentials(
        connections = listOf(validConnection),
        data = mutableMapOf(
            CURRENCY_API_TOKEN to "currencyToken",
            STOCK_API_TOKEN to "stockToken"
        )
    )

    private lateinit var getProfileServer: MockWebServer
    private lateinit var currencyConversionServer: MockWebServer

    @BeforeEach
    fun setup() {
        getProfileServer = MockWebServer()
        currencyConversionServer = MockWebServer()
    }

    @AfterEach
    fun after() {
        getProfileServer.shutdown()
        currencyConversionServer.shutdown()
    }

    @ParameterizedTest(name = "setup - [${ParameterizedTest.INDEX_PLACEHOLDER}] missing ${ParameterizedTest.ARGUMENTS_WITH_NAMES_PLACEHOLDER}")
    @ValueSource(strings = [CURRENCY_API_TOKEN, STOCK_API_TOKEN])
    fun `setup - return false when connection missing required data param`(token: String) = runTest {
        val credentials = credentials.copy(data = credentials.data.minus(token))

        assertFalse(StockTrackerPlugin(client, moshi, credentials).setup())
    }

    @Test
    fun `setup - return false when no usable connections`() = runTest {
        val invalid1 = validConnection.copy(name = "name1", data = validConnection.data.minus(COUNT))
        val invalid2 = validConnection.copy(name = "name2", data = validConnection.data.minus(CURRENCY))
        val invalid3 = validConnection.copy(name = "name3", data = validConnection.data.minus(TICKER))
        val invalid4 = validConnection.copy(name = "name4", data = validConnection.data.plus(COUNT to "-10"))
        val invalid5 = validConnection.copy(name = "name5", data = validConnection.data.plus(COUNT to "abc"))

        val invalidConnections = listOf(invalid1, invalid2, invalid3, invalid4, invalid5)
        val credentials = credentials.copy(connections = invalidConnections)

        assertFalse(StockTrackerPlugin(client, moshi, credentials).setup())
    }

    @Test
    fun `setup - required data and at least one valid connection`() = runTest {
        assertTrue(StockTrackerPlugin(client, moshi, credentials).setup())
    }

    @Test
    fun getBanyPluginBudgetAccountIds() = runTest {
        val expected = BanyPluginBudgetAccountIds(
            ynabBudgetId = validConnection.ynabBudgetId,
            ynabAccountId = validConnection.ynabAccountId,
        )

        val plugin = StockTrackerPlugin(client, moshi, credentials)
            .apply { setup() }

        assertEquals(listOf(expected), plugin.getBanyPluginBudgetAccountIds())
    }

    @Test
    fun getNewBanyPluginTransactionsSince() = runTest {
        val marketPrice = BigDecimal("219.23")
        val conversion = BigDecimal("1.275715")
        val count = BigDecimal(validConnection.data.getValue(COUNT))
        val expected = marketPrice * conversion * count

        val getProfileJson = File(RESOURCES_FILE, "get-profile.json").readText()
        getProfileServer.enqueue(MockResponse().setBody(getProfileJson))
        getProfileServer.start(8080)

        val currencyConversionJson = File(RESOURCES_FILE, "currency-conversion.json").readText()
        currencyConversionServer.enqueue(MockResponse().setBody(currencyConversionJson))
        currencyConversionServer.start(8081)

        val results = with(
            StockTrackerPlugin(
                client,
                moshi,
                credentials,
                getProfileServer.url("/"),
                currencyConversionServer.url("/")
            )
        ) {
            setup()
            getNewBanyPluginTransactionsSince(
                BanyPluginBudgetAccountIds(
                    ynabBudgetId = validConnection.ynabBudgetId,
                    ynabAccountId = validConnection.ynabAccountId
                ),
                null
            )
        }

        assertEquals(1, getProfileServer.requestCount)
        assertEquals(1, currencyConversionServer.requestCount)

        assertEquals(1, results.size)
        assertEquals(expected, results.first().amount)

        // GET 200 /stock/v2/get-profile
        val getProfile = getProfileServer.takeRequest(TIMEOUT, TimeUnit.SECONDS) ?: fail()
        assertEquals("GET", getProfile.method)
        assertTrue(getProfile.path?.contains("symbol=${validConnection.data[TICKER]}") ?: false)

        // GET 200 /api/v7/convert
        val getConversion = currencyConversionServer.takeRequest(TIMEOUT, TimeUnit.SECONDS) ?: fail()
        assertEquals("GET", getConversion.method)
        assertTrue(getConversion.path?.contains("q=USD_${validConnection.data[CURRENCY]?.uppercase()}") ?: false)
    }

    @Test
    fun `getNewBanyPluginTransactionsSince - return empty list when currency pair not found in response`() = runTest {
        val getProfileJson = File(RESOURCES_FILE, "get-profile.json").readText()
        getProfileServer.enqueue(MockResponse().setBody(getProfileJson))
        getProfileServer.start(8080)

        val currencyConversionJson = "{}"
        currencyConversionServer.enqueue(MockResponse().setBody(currencyConversionJson))
        currencyConversionServer.start(8081)

        val results = with(
            StockTrackerPlugin(
                client,
                moshi,
                credentials,
                getProfileServer.url("/"),
                currencyConversionServer.url("/")
            )
        ) {
            setup()
            getNewBanyPluginTransactionsSince(
                BanyPluginBudgetAccountIds(
                    ynabBudgetId = validConnection.ynabBudgetId,
                    ynabAccountId = validConnection.ynabAccountId
                ),
                null
            )
        }

        assertEquals(1, getProfileServer.requestCount)
        assertEquals(1, currencyConversionServer.requestCount)

        assertTrue(results.isEmpty())
    }

    @Test
    fun `getNewBanyPluginTransactionsSince - return empty list when get-profile GET unsuccessful`() = runTest {
        getProfileServer.enqueue(MockResponse().setResponseCode(401))
        getProfileServer.start(8080)

        currencyConversionServer.start(8081)

        val results = with(
            StockTrackerPlugin(
                client,
                moshi,
                credentials,
                getProfileServer.url("/"),
                currencyConversionServer.url("/")
            )
        ) {
            setup()
            getNewBanyPluginTransactionsSince(
                BanyPluginBudgetAccountIds(
                    ynabBudgetId = validConnection.ynabBudgetId,
                    ynabAccountId = validConnection.ynabAccountId
                ),
                null
            )
        }

        assertEquals(1, getProfileServer.requestCount)
        assertEquals(0, currencyConversionServer.requestCount)

        assertTrue(results.isEmpty())
    }

    @Test
    fun `getNewBanyPluginTransactionsSince - return empty list when get-profile response has no body`() = runTest {
        getProfileServer.enqueue(MockResponse().setResponseCode(200))
        getProfileServer.start(8080)

        currencyConversionServer.start(8081)

        val results = with(
            StockTrackerPlugin(
                client,
                moshi,
                credentials,
                getProfileServer.url("/"),
                currencyConversionServer.url("/")
            )
        ) {
            setup()
            getNewBanyPluginTransactionsSince(
                BanyPluginBudgetAccountIds(
                    ynabBudgetId = validConnection.ynabBudgetId,
                    ynabAccountId = validConnection.ynabAccountId
                ),
                null
            )
        }

        assertEquals(1, getProfileServer.requestCount)
        assertEquals(0, currencyConversionServer.requestCount)

        assertTrue(results.isEmpty())
    }

    @Test
    fun `getNewBanyPluginTransactionsSince - return empty list when unable to parse get-profile json response`() = runTest {
        getProfileServer.enqueue(MockResponse().setBody("foo"))
        getProfileServer.start(8080)

        currencyConversionServer.start(8081)

        val results = with(
            StockTrackerPlugin(
                client,
                moshi,
                credentials,
                getProfileServer.url("/"),
                currencyConversionServer.url("/")
            )
        ) {
            setup()
            getNewBanyPluginTransactionsSince(
                BanyPluginBudgetAccountIds(
                    ynabBudgetId = validConnection.ynabBudgetId,
                    ynabAccountId = validConnection.ynabAccountId
                ),
                null
            )
        }

        assertEquals(1, getProfileServer.requestCount)
        assertEquals(0, currencyConversionServer.requestCount)

        assertTrue(results.isEmpty())
    }

    @Test
    fun `getNewBanyPluginTransactionsSince - return empty list when currency-conversion GET unsuccessful`() = runTest {
        val getProfileJson = File(RESOURCES_FILE, "get-profile.json").readText()
        getProfileServer.enqueue(MockResponse().setBody(getProfileJson))
        getProfileServer.start(8080)

        currencyConversionServer.enqueue(MockResponse().setResponseCode(401))
        currencyConversionServer.start(8081)

        val results = with(
            StockTrackerPlugin(
                client,
                moshi,
                credentials,
                getProfileServer.url("/"),
                currencyConversionServer.url("/")
            )
        ) {
            setup()
            getNewBanyPluginTransactionsSince(
                BanyPluginBudgetAccountIds(
                    ynabBudgetId = validConnection.ynabBudgetId,
                    ynabAccountId = validConnection.ynabAccountId
                ),
                null
            )
        }

        assertEquals(1, getProfileServer.requestCount)
        assertEquals(1, currencyConversionServer.requestCount)

        assertTrue(results.isEmpty())
    }

    @Test
    fun `getNewBanyPluginTransactionsSince - return empty list when currency-conversion has no body`() = runTest {
        val getProfileJson = File(RESOURCES_FILE, "get-profile.json").readText()
        getProfileServer.enqueue(MockResponse().setBody(getProfileJson))
        getProfileServer.start(8080)

        currencyConversionServer.enqueue(MockResponse().setResponseCode(200))
        currencyConversionServer.start(8081)

        val results = with(
            StockTrackerPlugin(
                client,
                moshi,
                credentials,
                getProfileServer.url("/"),
                currencyConversionServer.url("/")
            )
        ) {
            setup()
            getNewBanyPluginTransactionsSince(
                BanyPluginBudgetAccountIds(
                    ynabBudgetId = validConnection.ynabBudgetId,
                    ynabAccountId = validConnection.ynabAccountId
                ),
                null
            )
        }

        assertEquals(1, getProfileServer.requestCount)
        assertEquals(1, currencyConversionServer.requestCount)

        assertTrue(results.isEmpty())
    }

    @Test
    fun `getNewBanyPluginTransactionsSince - return empty list when unable to parse currency-conversion json response`() = runTest {
        val getProfileJson = File(RESOURCES_FILE, "get-profile.json").readText()
        getProfileServer.enqueue(MockResponse().setBody(getProfileJson))
        getProfileServer.start(8080)

        currencyConversionServer.enqueue(MockResponse().setBody("foo"))
        currencyConversionServer.start(8081)

        val results = with(
            StockTrackerPlugin(
                client,
                moshi,
                credentials,
                getProfileServer.url("/"),
                currencyConversionServer.url("/")
            )
        ) {
            setup()
            getNewBanyPluginTransactionsSince(
                BanyPluginBudgetAccountIds(
                    ynabBudgetId = validConnection.ynabBudgetId,
                    ynabAccountId = validConnection.ynabAccountId
                ),
                null
            )
        }

        assertEquals(1, getProfileServer.requestCount)
        assertEquals(1, currencyConversionServer.requestCount)

        assertTrue(results.isEmpty())
    }

    // Adapter used by integration test
    @JsonClass(generateAdapter = true)
    data class Credentials(
        override val connections: List<Connection>,
        override val data: Map<String, String>
    ) : BanyPlugin.Credentials {
        override val username: String = ""
        override val password: String = ""
    }

    // Adapter used by integration test
    @JsonClass(generateAdapter = true)
    data class Connection(
        override val name: String,
        override val ynabBudgetId: String,
        override val ynabAccountId: String,
        override val data: Map<String, String> = mutableMapOf()
    ) : BanyPlugin.Connection {
        override val thirdPartyAccountId: String = ""
    }
}