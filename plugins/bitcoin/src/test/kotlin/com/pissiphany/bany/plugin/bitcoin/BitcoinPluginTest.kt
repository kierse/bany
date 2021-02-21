package com.pissiphany.bany.plugin.bitcoin

import com.pissiphany.bany.plugin.BanyPlugin
import com.pissiphany.bany.plugin.bitcoin.adapter.BigDecimalAdapter
import com.pissiphany.bany.plugin.dataStructure.BanyPluginBudgetAccountIds
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.File
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

private const val COIN_ID = "coinId"
private const val AMOUNT = "amount"
private const val CURRENCY = "currency"

private val RESOURCES_FILE = File("src/test/resources/json")

private const val TIMEOUT = 0L

class BitcoinPluginTest {
    private lateinit var server: MockWebServer
    private lateinit var client: OkHttpClient
    private lateinit var moshi: Moshi
    private lateinit var credentials: Credentials

    @BeforeEach
    fun setup() {
        server = MockWebServer()

        client = OkHttpClient
            .Builder()
            .build()

        moshi = Moshi.Builder()
            .add(BigDecimalAdapter())
            .build()

        credentials = Credentials(
            connections = listOf(
                Connection(
                    ynabBudgetId = "budget1",
                    ynabAccountId = "account1",
                    data = mutableMapOf(
                        COIN_ID to "bitcoin",
                        AMOUNT to "1.5",
                        CURRENCY to "cad",
                    )
                ),
                Connection(
                    ynabBudgetId = "budget1",
                    ynabAccountId = "account2",
                    data = mutableMapOf(
                        COIN_ID to "bitcoin-cash",
                        AMOUNT to "0.5",
                        CURRENCY to "usd",
                    )
                )
            )
        )
    }

    @AfterEach
    fun after() {
        server.shutdown()
    }

    @Test
    fun `constructor - unsupported coin type`() {
        credentials.connections.last().data[COIN_ID] = "foo"
        assertThrows<IllegalStateException> { BitcoinPlugin(credentials, client, moshi) }
    }

    @ParameterizedTest
    @ValueSource(strings = [AMOUNT, CURRENCY, COIN_ID])
    fun `constructor - missing config property`(missing: String) {
        credentials.connections.last().data.remove(missing)
        assertThrows<IllegalStateException> { BitcoinPlugin(credentials, client, moshi) }
    }

    @Test
    fun getBanyPluginBudgetAccountIds() {
        val results = BitcoinPlugin(credentials, client, moshi).getBanyPluginBudgetAccountIds()

        assertEquals(
            listOf(
                BanyPluginBudgetAccountIds(
                    ynabBudgetId = "budget1",
                    ynabAccountId = "account1"
                ),
                BanyPluginBudgetAccountIds(
                    ynabBudgetId = "budget1",
                    ynabAccountId = "account2"
                )
            ),
            results
        )
    }

    @ParameterizedTest
    @ValueSource(ints = [0,1])
    fun getNewBanyPluginTransactionsSince(index: Int) {
        val json = File(RESOURCES_FILE, "simple_price.json").readText()
        val priceModel = moshi.adapter(SimplePrice::class.java).run { fromJson(json) } ?: fail()

        server.enqueue(MockResponse().setBody(json))
        server.start()

        with(credentials.connections[index]) {
            val expectedCoinId = data.getValue(COIN_ID)
            val expectedCurrency = data.getValue(CURRENCY)
            val givenAmount = when(expectedCoinId) {
                "bitcoin" -> priceModel.bitcoin.getValue(expectedCurrency)
                else -> priceModel.bitcoinCash.getValue(expectedCurrency)
            }
            val expectedAmount = BigDecimal(data.getValue(AMOUNT)) * givenAmount

            val plugin = BitcoinPlugin(credentials, client, moshi, server.url("/"))

            val results = plugin.getNewBanyPluginTransactionsSince(
                BanyPluginBudgetAccountIds(ynabBudgetId = ynabBudgetId, ynabAccountId = ynabAccountId), null
            )

            assertEquals(1, results.size)
            assertEquals(expectedAmount, results.first().amount)

            // GET 200 /api/v3/simple/price
            val getPrice = server.takeRequest(TIMEOUT, TimeUnit.SECONDS) ?: fail()
            assertEquals("GET", getPrice.method)
            assertTrue(getPrice.path?.contains("=$expectedCoinId") ?: false)
            assertTrue(getPrice.path?.contains("=$expectedCurrency") ?: false)
        }
    }

    // Adapter used by integration test
    @JsonClass(generateAdapter = true)
    data class Credentials(
        override val connections: List<Connection>,
    ) : BanyPlugin.Credentials {
        override val username: String = ""
        override val password: String = ""
        override val data: Map<String, String> = emptyMap()
    }

    // Adapter used by integration test
    @JsonClass(generateAdapter = true)
    data class Connection(
        override val ynabBudgetId: String,
        override val ynabAccountId: String,
        override val data: MutableMap<String, String> = mutableMapOf()
    ) : BanyPlugin.Connection {
        override val thirdPartyAccountId: String = ""
    }
}