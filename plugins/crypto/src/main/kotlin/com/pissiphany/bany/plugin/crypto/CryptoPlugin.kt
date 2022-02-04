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
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset

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

    override fun setup(): Boolean {
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

    override fun getNewBanyPluginTransactionsSince(
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

        client.value.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unable to fetch currency conversion: $response")
            val body = response.body ?: throw IOException("No body found!")
            val simplePrice = adapter.fromJson(body.source()) ?: throw IOException("Unable to parse response!")

            val calculatedValue = when(connection.data[COIN_ID]) {
                "bitcoin-cash" -> calculateValue(simplePrice.bitcoinCash, amount, currency)
                "bitcoin" -> calculateValue(simplePrice.bitcoin, amount, currency)
                "ethereum" -> calculateValue(simplePrice.ethereum, amount, currency)
                else -> throw IllegalArgumentException("Unsupported crypto: $COIN_ID")
            }

            return listOf(
                BanyPluginAccountBalance(
                    date = OffsetDateTime.now(ZoneOffset.UTC),
                    payee = "",
                    amount = calculatedValue
                )
            )
        }
    }

    private fun getConnection(budgetAccountIds: BanyPluginBudgetAccountIds): BanyPlugin.Connection {
        return connections
            .first { it.ynabBudgetId == budgetAccountIds.ynabBudgetId && it.ynabAccountId == budgetAccountIds.ynabAccountId }
    }
}

private fun calculateValue(currencyToPrice: Map<String, BigDecimal>, amount: BigDecimal, currency: String): BigDecimal {
    return currencyToPrice[currency]
        ?.let { it * amount }
        ?: throw IOException("Requested currency missing: $currency")
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