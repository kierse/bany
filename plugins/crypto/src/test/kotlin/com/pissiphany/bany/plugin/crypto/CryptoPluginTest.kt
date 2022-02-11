package com.pissiphany.bany.plugin.crypto

import com.pissiphany.bany.plugin.BanyPlugin
import com.pissiphany.bany.plugin.crypto.adapter.BigDecimalAdapter
import com.pissiphany.bany.plugin.dataStructure.BanyPluginBudgetAccountIds
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
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

/**
 *  API query for test data:
 *  https://api.coingecko.com/api/v3/simple/price?ids=bitcoin,bitcoin-cash,ethereum&vs_currencies=cad,usd
 */
private val RESOURCES_FILE = File("src/test/resources/json")

private const val TIMEOUT = 0L

@OptIn(ExperimentalCoroutinesApi::class)
class CryptoPluginTest {
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

    private val validConnection1 = Connection(
        name = "name1",
        ynabBudgetId = "budget1",
        ynabAccountId = "account1",
        data = mutableMapOf(
            COIN_ID to "bitcoin",
            AMOUNT to "1.5",
            CURRENCY to "cad",
        )
    )

    private val validConnection2 = Connection(
        name = "name2",
        ynabBudgetId = "budget1",
        ynabAccountId = "account2",
        data = mutableMapOf(
            COIN_ID to "bitcoin-cash",
            AMOUNT to "0.5",
            CURRENCY to "usd",
        )
    )

    private val validConnection3 = Connection(
        name = "name3",
        ynabBudgetId = "budget1",
        ynabAccountId = "account3",
        data = mutableMapOf(
            COIN_ID to "ethereum",
            AMOUNT to "2",
            CURRENCY to "cad",
        )
    )

    private val credentials = Credentials(connections = listOf(validConnection1, validConnection2, validConnection3))

    private lateinit var server: MockWebServer

    @BeforeEach
    fun setup() {
        server = MockWebServer()
    }

    @AfterEach
    fun after() {
        server.shutdown()
    }

    @Test
    fun `setup - required data and at least one valid connection`() = runTest {
        assertTrue(CryptoPlugin(client, moshi, credentials).setup())
    }

    @ParameterizedTest(name = "setup - [${ParameterizedTest.INDEX_PLACEHOLDER}] missing ${ParameterizedTest.ARGUMENTS_WITH_NAMES_PLACEHOLDER}")
    @ValueSource(strings = [AMOUNT, CURRENCY, COIN_ID])
    fun `setup - return false when connection missing required data`(key: String) = runTest {
        val connection = validConnection1.copy(data = validConnection1.data.minus(key))
        val credentials = credentials.copy(connections = listOf(connection))

        assertFalse(CryptoPlugin(client, moshi, credentials).setup())
    }

    @Test
    fun getBanyPluginBudgetAccountIds() = runTest {
        val credentials = credentials.copy(connections = listOf(validConnection1, validConnection2, validConnection3))
        val results = CryptoPlugin(client, moshi, credentials).getBanyPluginBudgetAccountIds()

        assertEquals(
            listOf(
                BanyPluginBudgetAccountIds(
                    ynabBudgetId = "budget1",
                    ynabAccountId = "account1"
                ),
                BanyPluginBudgetAccountIds(
                    ynabBudgetId = "budget1",
                    ynabAccountId = "account2"
                ),

                BanyPluginBudgetAccountIds(
                    ynabBudgetId = "budget1",
                    ynabAccountId = "account3"
                )
            ),
            results
        )
    }

    @ParameterizedTest(name = "getNewBanyPluginTransactionsSince - [${ParameterizedTest.INDEX_PLACEHOLDER}] connection ${ParameterizedTest.ARGUMENTS_WITH_NAMES_PLACEHOLDER}")
    @ValueSource(ints = [0,1,2])
    fun getNewBanyPluginTransactionsSince(index: Int) = runTest {
        val json = File(RESOURCES_FILE, "simple_price.json").readText()
        val priceModel = moshi.value.adapter(SimplePrice::class.java).run { fromJson(json) } ?: fail()

        server.enqueue(MockResponse().setBody(json))
        server.start()

        with(credentials.connections[index]) {
            val expectedCoinId = data.getValue(COIN_ID)
            val expectedCurrency = data.getValue(CURRENCY)
            val givenAmount = when(expectedCoinId) {
                "bitcoin" -> priceModel.bitcoin.getValue(expectedCurrency)
                "bitcoin-cash" -> priceModel.bitcoinCash.getValue(expectedCurrency)
                "ethereum" -> priceModel.ethereum.getValue(expectedCurrency)
                else -> fail("unsupported crypto: $expectedCoinId")
            }
            val expectedAmount = BigDecimal(data.getValue(AMOUNT)) * givenAmount

            val results = with(CryptoPlugin(client, moshi, credentials, server.url("/"))) {
                setup()
                getNewBanyPluginTransactionsSince(
                    BanyPluginBudgetAccountIds(ynabBudgetId = ynabBudgetId, ynabAccountId = ynabAccountId), null
                )
            }

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
        override val name: String,
        override val ynabBudgetId: String,
        override val ynabAccountId: String,
        override val data: Map<String, String> = mutableMapOf()
    ) : BanyPlugin.Connection {
        override val thirdPartyAccountId: String = ""
    }
}