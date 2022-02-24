package com.pissiphany.bany.plugin.stock

import com.pissiphany.bany.plugin.BanyConfigurablePlugin
import com.pissiphany.bany.plugin.BanyPlugin
import com.pissiphany.bany.plugin.dataStructure.BanyPluginAccountBalance
import com.pissiphany.bany.plugin.dataStructure.BanyPluginBudgetAccountIds
import com.pissiphany.bany.plugin.dataStructure.BanyPluginTransaction
import com.pissiphany.bany.plugin.stock.StockTrackerPlugin.Companion.COUNT
import com.pissiphany.bany.plugin.stock.StockTrackerPlugin.Companion.CURRENCY
import com.pissiphany.bany.plugin.stock.StockTrackerPlugin.Companion.TICKER
import com.pissiphany.bany.shared.fetch
import com.pissiphany.bany.shared.logger
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import mu.KLogger
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okio.IOException
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset

private const val ROOT_STOCK_URL = "https://apidojo-yahoo-finance-v1.p.rapidapi.com"
private const val ROOT_CURRENCY_URL = "https://free.currconv.com"

class StockTrackerPlugin(
    private val client: Lazy<OkHttpClient>,
    private val moshi: Lazy<Moshi>,
    credentials: BanyPlugin.Credentials,
    stockServerRoot: HttpUrl? = null,
    currencyServerRoot: HttpUrl? = null
) : BanyConfigurablePlugin {
    internal companion object {
        const val TICKER = "ticker"
        const val COUNT = "count"
        const val CURRENCY = "currency"
        const val STOCK_API_TOKEN = "stockApiToken"
        const val CURRENCY_API_TOKEN = "currencyApiToken"
    }

    private val stockRoot = stockServerRoot ?: ROOT_STOCK_URL.toHttpUrl()
    private val currencyRoot = currencyServerRoot ?: ROOT_CURRENCY_URL.toHttpUrl()
    private val logger by logger()

    private val credentialsData = credentials.data
    private val connections: List<BanyPlugin.Connection> = credentials.connections
        .filter { verifyRequiredData(it, logger) }

    private lateinit var profileAdapter: JsonAdapter<GetProfile>
    private lateinit var currencyAdapter: JsonAdapter<Map<String, BigDecimal>>

    override suspend fun setup(): Boolean {
        super.setup()

        for (key in listOf(CURRENCY_API_TOKEN, STOCK_API_TOKEN)) {
            if (!credentialsData[key].isNullOrEmpty()) continue
            logger.warn { "Skipping ${javaClass.simpleName}. Must provide value for: '$key'" }
            return false
        }

        if (connections.isEmpty()) {
            logger.info("Skipping ${javaClass.simpleName}, no usable connections")
            return false
        }

        profileAdapter = moshi.value.adapter(GetProfile::class.java)
        currencyAdapter = Types
            .newParameterizedType(Map::class.java, String::class.java, BigDecimal::class.java)
            .let(moshi.value::adapter)

        return true
    }

    override fun getBanyPluginBudgetAccountIds(): List<BanyPluginBudgetAccountIds> {
        return connections.map { BanyPluginBudgetAccountIds(ynabBudgetId = it.ynabBudgetId, ynabAccountId = it.ynabAccountId) }
    }

    override suspend fun getNewBanyPluginTransactionsSince(
        budgetAccountIds: BanyPluginBudgetAccountIds, date: LocalDate?
    ): List<BanyPluginTransaction> {
        val connection = with(budgetAccountIds) {
            connections.first { it.ynabBudgetId == ynabBudgetId && it.ynabAccountId == ynabAccountId }
        }

        val profile = fetchProfile(connection) ?: return emptyList()
        val currencyPair = "${profile.price.currency.uppercase()}_${connection.data.getValue(CURRENCY).uppercase()}"
        val currencyMap = fetchCurrencyMap(currencyPair)

        val conversion = currencyMap[currencyPair]
        if (conversion == null) {
            logger.warn("Unable to get currency conversion: $currencyPair not found in response!")
            return emptyList()
        }

        val count = BigDecimal(connection.data[COUNT])
        return listOf(
            BanyPluginAccountBalance(
                date = OffsetDateTime.now(ZoneOffset.UTC),
                payee = "",
                amount = profile.price.regularMarketPrice.raw * conversion * count
            )
        )
    }

    private suspend fun fetchProfile(connection: BanyPlugin.Connection): GetProfile? {
        val getProfileUrl = stockRoot.newBuilder()
            .addPathSegments("stock/v2/get-profile")
            .addQueryParameter("region", "US")
            .addQueryParameter("symbol", connection.data[TICKER])
            .build()
        val getProfileRequest = Request.Builder()
            .get()
            .url(getProfileUrl)
            .addHeader("x-rapidapi-host", "apidojo-yahoo-finance-v1.p.rapidapi.com")
            .addHeader("x-rapidapi-key", credentialsData.getValue(STOCK_API_TOKEN))
            .build()

        val response = try {
            client.value.fetch(getProfileRequest)
        } catch (e: IOException) {
            logger.warn("Unable to GET stock profile: ${e.message}")
            return null
        }

        return profileAdapter.process(response)
    }

    private suspend fun fetchCurrencyMap(currencyPair: String): Map<String, BigDecimal> {
        val getCurrencyConversionUrl = currencyRoot.newBuilder()
            .addPathSegments("api/v7/convert")
            .addQueryParameter("compact", "ultra")
            .addQueryParameter("apiKey", credentialsData[CURRENCY_API_TOKEN])
            .addQueryParameter("q", currencyPair)
            .build()
        val getCurrencyRequestRequest = Request.Builder()
            .get()
            .url(getCurrencyConversionUrl)
            .build()

        val response = try {
            client.value.fetch(getCurrencyRequestRequest)
        } catch (e: IOException) {
            logger.warn("Unable to GET currency conversion: ${e.message}")
            return emptyMap()
        }

        return currencyAdapter.process(response) ?: emptyMap()
    }

    private fun <T> JsonAdapter<T>.process(response: Response): T? {
        response.use {
            if (!it.isSuccessful) {
                logger.warn("Request unsuccessful: (HTTP ${it.code}) ${it.message}")
                return null
            }

            val bodySource = it.body?.source()
            if (bodySource == null) {
                logger.warn("Response has empty body!")
                return null
            }

            val result = try {
                fromJson(bodySource)
            } catch(e: IOException) {
                logger.error("Unable to parse response body: ${e.message}")
                return null
            }

            if (result == null) {
                logger.warn("Unable to parse response body!")
                return null
            }

            return result
        }
    }
}

private fun verifyRequiredData(con: BanyPlugin.Connection, logger: KLogger): Boolean {
    for (key in listOf(COUNT, CURRENCY, TICKER)) {
        if (!con.data[key].isNullOrEmpty()) continue
        logger.warn { "Skipping connection '${con.name}'. Must provide value for: '$key'" }
        return false
    }

    val count = try {
        BigDecimal(con.data[COUNT])
    } catch (_: NumberFormatException) {
        BigDecimal.ZERO
    }

    if (count <= BigDecimal.ZERO) {
        logger.warn { "Skipping connection '${con.name}'. Count must be a valid number greater than 0!" }
        return false
    }

    return true
}

@JsonClass(generateAdapter = true)
internal data class GetProfile(
    val price: ProfilePrice
)

@JsonClass(generateAdapter = true)
internal data class ProfilePrice(
    val currency: String,
    val regularMarketPrice: RegularMarketPrice
)

@JsonClass(generateAdapter = true)
internal data class RegularMarketPrice(val raw: BigDecimal)
