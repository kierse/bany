package com.pissiphany.bany.plugin.bitcoin

import com.pissiphany.bany.plugin.BanyConfigurablePlugin
import com.pissiphany.bany.plugin.BanyPlugin
import com.pissiphany.bany.plugin.dataStructure.BanyPluginAccountBalance
import com.pissiphany.bany.plugin.dataStructure.BanyPluginBudgetAccountIds
import com.pissiphany.bany.plugin.dataStructure.BanyPluginTransaction
import com.squareup.moshi.Json
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
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

private val supportedCoins = setOf("bitcoin", "bitcoin-cash")

class BitcoinPlugin(
    private val credentials: BanyPlugin.Credentials,
    private val client: OkHttpClient,
    moshi: Moshi,
    serverRoot: HttpUrl? = null
) : BanyConfigurablePlugin {
    private val adapter: JsonAdapter<SimplePrice>
    private val root = serverRoot ?: ROOT_URL.toHttpUrl()

    init {
        credentials.connections.forEach { con ->
            checkNotNull(con.data[AMOUNT]) { "Must provide $AMOUNT!" }
            checkNotNull(con.data[CURRENCY]) { "Must indicate desired $CURRENCY!" }
            checkNotNull(con.data[COIN_ID]) { "Must indicate $COIN_ID!" }
            check(supportedCoins.contains(con.data[COIN_ID])) { "Unsupported $COIN_ID!" }
        }

        adapter = moshi.adapter(SimplePrice::class.java)
    }

    override fun getBanyPluginBudgetAccountIds(): List<BanyPluginBudgetAccountIds> {
        return credentials.connections
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

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unable to fetch currency conversion: $response")
            val body = response.body ?: throw IOException("No body found!")
            val simplePrice = adapter.fromJson(body.source()) ?: throw IOException("Unable to parse response!")

            val calculatedValue = when(connection.data[COIN_ID]) {
                "bitcoin-cash" -> calculateValue(simplePrice.bitcoinCash, amount, currency)
                else /* bitcoin */ -> calculateValue(simplePrice.bitcoin, amount, currency)
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
        return credentials.connections
            .filter { it.ynabBudgetId == budgetAccountIds.ynabBudgetId }
            .first { it.ynabAccountId == budgetAccountIds.ynabAccountId }
    }
}

private fun calculateValue(currencyToPrice: Map<String, BigDecimal>, amount: BigDecimal, currency: String): BigDecimal {
    return currencyToPrice[currency]
        ?.let { it * amount }
        ?: throw IOException("Requested currency missing: $currency")
}

@JsonClass(generateAdapter = true)
internal data class SimplePrice(
    val bitcoin: Map<String, BigDecimal> = emptyMap(),

    @Json(name = "bitcoin-cash")
    val bitcoinCash: Map<String, BigDecimal> = emptyMap()
)