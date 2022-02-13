package com.pissiphany.bany.plugin.crypto

import com.pissiphany.bany.plugin.BanyConfigurablePlugin
import com.pissiphany.bany.plugin.BanyPlugin
import com.pissiphany.bany.plugin.dataStructure.BanyPluginAccountBalance
import com.pissiphany.bany.plugin.dataStructure.BanyPluginBudgetAccountIds
import com.pissiphany.bany.plugin.dataStructure.BanyPluginTransaction
import com.pissiphany.bany.shared.logger
import com.squareup.moshi.Json
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import mu.KLogger
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okio.IOException
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

private const val COIN_ID = "coinId"
private const val AMOUNT = "amount"
private const val CURRENCY = "currency"
private const val ROOT_URL = "https://api.coingecko.com"

private val supportedCoins = setOf("bitcoin", "bitcoin-cash", "ethereum")

class CryptoPlugin(
    private val client: Lazy<OkHttpClient>,
    private val moshi: Lazy<Moshi>,
    credentials: BanyPlugin.Credentials,
    serverRoot: HttpUrl? = null
) : BanyConfigurablePlugin {
    private val root = serverRoot ?: ROOT_URL.toHttpUrl()
    private val logger by logger()

    private val connections: List<BanyPlugin.Connection> = credentials.connections
        .filter { verifyRequiredData(it, logger) }

    private lateinit var adapter: JsonAdapter<SimplePrice>

    override suspend fun setup(): Boolean {
        super.setup()

        if (connections.isEmpty()) {
            logger.info("Skipping ${javaClass.simpleName}, no usable connections")
            return false
        }

        adapter = moshi.value.adapter(SimplePrice::class.java)
        return true
    }

    override fun getBanyPluginBudgetAccountIds(): List<BanyPluginBudgetAccountIds> {
        return connections
            .map { BanyPluginBudgetAccountIds(ynabBudgetId = it.ynabBudgetId, ynabAccountId = it.ynabAccountId) }
    }

    override suspend fun getNewBanyPluginTransactionsSince(
        budgetAccountIds: BanyPluginBudgetAccountIds,
        date: LocalDate?
    ): List<BanyPluginTransaction> {
        val connection = getConnection(budgetAccountIds)
        val amount = connection.data.getValue(AMOUNT).toBigDecimal()
        val currency = connection.data.getValue(CURRENCY).toLowerCase()

        val getPrice = root.newBuilder()
            .addPathSegments("api/v3/simple/price")
            .addQueryParameter("ids", connection.data[COIN_ID])
            .addQueryParameter("vs_currencies", connection.data[CURRENCY])
            .build()
        val request = Request.Builder()
            .get()
            .url(getPrice)
            .build()

        val response = try {
            request.fetch()
        } catch (e: IOException) {
            logger.warn("Unable to GET crypto price: ${e.message}")
            return emptyList()
        }
        val simplePrice = response.process(adapter) ?: return emptyList()

        val calculatedValue = calculateValue(simplePrice, connection.data.getValue(COIN_ID), currency, amount)
            ?: return emptyList()

        return listOf(
            BanyPluginAccountBalance(
                date = OffsetDateTime.now(ZoneOffset.UTC),
                payee = "",
                amount = calculatedValue
            )
        )
    }

    private suspend fun Request.fetch(): Response = suspendCoroutine { cont ->
        client.value.newCall(this).enqueue(object : Callback {
            override fun onFailure(call: Call, e: java.io.IOException) {
                cont.resumeWithException(e)
            }

            override fun onResponse(call: Call, response: Response) {
                cont.resume(response)
            }
        })
    }

    private fun <T> Response.process(adapter: JsonAdapter<T>): T? = use { resp ->
        if (!resp.isSuccessful) {
            logger.warn("Request unsuccessful: (HTTP ${resp.code}) ${resp.message}")
            return null
        }

        val bodySource = resp.body?.source()
        if (bodySource == null) {
            logger.warn("Response has empty body!")
            return null
        }

        val result = try {
            adapter.fromJson(bodySource)
        } catch(e: java.io.IOException) {
            logger.error("Unable to parse response body: ${e.message}")
            return null
        }

        if (result == null) {
            logger.warn("Unable to parse response body!")
            return null
        }

        return result
    }

    private fun calculateValue(simplePrice: SimplePrice, coin: String, currency: String, amount: BigDecimal): BigDecimal? {
        val price = when(coin) {
            "bitcoin-cash" -> simplePrice.bitcoinCash[currency]
            "bitcoin" -> simplePrice.bitcoin[currency]
            "ethereum" -> simplePrice.ethereum[currency]
            else -> {
                logger.error("Unknown/unsupported coin type: '$coin'")
                null
            }
        } ?: return null

        return price * amount
    }

    private fun getConnection(budgetAccountIds: BanyPluginBudgetAccountIds): BanyPlugin.Connection {
        return connections
            .first { it.ynabBudgetId == budgetAccountIds.ynabBudgetId && it.ynabAccountId == budgetAccountIds.ynabAccountId }
    }
}

private fun verifyRequiredData(con: BanyPlugin.Connection, logger: KLogger): Boolean {
    for (key in listOf(AMOUNT, CURRENCY, COIN_ID)) {
        if (!con.data[key].isNullOrBlank()) continue
        logger.warn { "Skipping connection '${con.name}'. Must provide value for: '$key'" }
        return false
    }

    if (!supportedCoins.contains(con.data[COIN_ID])) {
        logger.warn { "Skipping connection '${con.name}'. '$COIN_ID' must be one of '$supportedCoins'" }
        return false
    }

    return true
}

@JsonClass(generateAdapter = true)
internal data class SimplePrice(
    val bitcoin: Map<String, BigDecimal> = emptyMap(),

    @Json(name = "bitcoin-cash")
    val bitcoinCash: Map<String, BigDecimal> = emptyMap(),

    val ethereum: Map<String, BigDecimal> = emptyMap(),
)