package com.pissiphany.bany.adapter.plugin

import com.pissiphany.bany.adapter.Constants.CONFIG_FILE
import com.pissiphany.bany.config.BanyConfig
import com.squareup.moshi.Moshi
import okhttp3.*
import org.junit.jupiter.api.Test
import java.net.CookieManager
import java.net.CookiePolicy
import java.time.LocalDate
import java.time.format.DateTimeFormatter

internal class SimpliiTest {
    private val BASE_URL = "https://online.simplii.com"
    private val STATIC_URL = "$BASE_URL/static/3a60cf428e5192970126258a37cefd2"
    private val LOGIN_URL = "$BASE_URL/ebm-resources/public/client/web/index.html"
    private val AUTH_URL = "$BASE_URL/ebm-anp/api/v1/json/sessions"
    private val ACCOUNTS_URL = "$BASE_URL/ebm-ai/api/v1/json/accounts"
    private val TRANSACTIONS_URL = "$BASE_URL/ebm-ai/api/v1/json/transactions"

    private val static_1_body = """
        // REDACTED
    """.trimIndent()

    private val static_2_body = """
        // REDACTED
    """.trimIndent()

    private data class AuthRequest(val card: Card, val password: String) {
        data class Card(val value: String, val description: String, val encrypted: Boolean, val encrypt: Boolean)
    }

    @Test
    fun test() {
        val moshi = Moshi.Builder().build()
        val configAdapter = moshi.adapter(BanyConfig::class.java)
        val config = configAdapter.fromJson(CONFIG_FILE.readText())
            ?: throw Exception("unable to load config!")

        val credentials = config.plugins["simplii"]?.first() ?: return // no simplii data, terminate

        val client = OkHttpClient
            .Builder()
            .cookieJar(QuotePreservingCookieJar(CookieManager(null, CookiePolicy.ACCEPT_ALL)))
            .build()

         seedCookieJar(client)

        var token = ""
        val code: Int
        val message: String

        try {
            // authenticate
            token = authenticate(client, moshi, credentials.username, credentials.password) ?: throw Exception("no token found!")

            // get accounts
            val accounts = getAccounts(client, moshi, token)
                .also {
                    for ((number, account) in it) {
                        println("$number => ${account.id}")
                    }
                }

            val account = accounts[credentials.connections.first().thirdPartyAccountId] ?: return
            getTransactions(client, moshi, token, account)
                .also {
                    println("found ${it.size} transaction(s)")
                    for (t in it) {
                        println("${t.id} => ${t.date}, ${t.date}, ${t.descriptionLine1}, ${t.transactionDescription}")
                    }
                }
        } finally {
            // terminate session
            if (token.isNotBlank()) {
                val result = terminateSession(client, token)
                code = result.first
                message = result.second

                println()
                println("===")
                when (code) {
                    204 -> println("successfully terminated session")
                    else -> throw Exception("$code: unable to terminate session. $message")
                }
            }
        }
    }

    private fun seedCookieJar(client: OkHttpClient, payload: String = static_1_body, count: Int = 2) {
        if (count <= 0) return

        val mediaType = MediaType.parse("text/plain;charset=UTF-8")
        val body1 = RequestBody.create(mediaType, payload)

        val request = Request.Builder()
            .url(STATIC_URL)
            .post(body1)
            .addHeader("accept", "*/*")
            .addHeader("accept-language", "en-US,en;q=0.9")
            .addHeader("content-type", "text/plain;charset=UTF-8")
            .addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:53.0) Gecko/20100101 Firefox/53.0")
            .build()

        println()
        println("seed cookie jar request")
        println(request.toString())
        printHeaders(request.headers())

        println()
        println("seed cookie jar response")

        client.newCall(request).execute().use { response ->
            if (response.code() != 201) throw Exception("error!")
        }

        // Note: must call it a second time for some reason
        seedCookieJar(client, static_2_body, count - 1)
    }

    private fun authenticate(client: OkHttpClient, moshi: Moshi, cardNumber: String, password: String): String? {
        val authAdapter = moshi.adapter(AuthRequest::class.java)
        val json = authAdapter.toJson(
            AuthRequest(
                card = AuthRequest.Card(
                    value = cardNumber,
                    description = "",
                    encrypted = false,
                    encrypt = true
                ),
                password = password
            )
        )

        val mediaType = MediaType.parse("application/vnd.api+json")
        val body = RequestBody.create(mediaType, json)

        val request = Request.Builder()
            .url(AUTH_URL)
            .post(body)
//            .addHeader("Cookie", "bm_sz=1C94067C461C54DF09CB8D55956F46B6~YAAQ15o7F61B5J9oAQAAUVIfMQJjy+no0hYzkgCB8jKuyleE8Uk2Lr+PmMUMdPRts6iKpbcto2L3DFgbCsdc7ehpzm2zItTd/6GxlMHlOekiMeCVStl6gt9qzR0QD073A9DNnh6wUYf7L3B7Bt6ZnU/wBu8DvKQMutxi40KOz138Z0pnwkTiJrtKq+OhOWeW")

            .addHeader("host", "online.simplii.com")
            .addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:53.0) Gecko/20100101 Firefox/53.0")
            .addHeader("accept", "application/vnd.api+json")
            .addHeader("accept-language", "en")
            .addHeader("accept-encoding", "gzip, deflate, br")
            .addHeader("referer", "https://online.simplii.com/ebm-resources/public/client/web/index.html")
            .addHeader("client-type", "default_web")
            .addHeader("brand", "pcf")
            .addHeader("X-Requested-With", "XMLHttpRequest")
            .addHeader("connection", "keep-alive")
            .addHeader("pragma", "no-cache")
            .addHeader("cache-control", "no-cache")

            .addHeader("Content-Type", "application/vnd.api+json")
            .addHeader("www-authenticate", "CardAndPassword")
            .addHeader("x-auth-token", "")
            .build()

        println()
        println("auth request")
        println(request.toString())
        printHeaders(request.headers())
        println(json)

        println()
        println("auth response")

        return client.newCall(request)
            .execute()
            .use { response ->
                println("code: ${response.code()}")
                printHeaders(response.headers())

                // return header or null
                response.header("x-auth-token")
            }
    }

    private fun getAccounts(client: OkHttpClient, moshi: Moshi, token: String) : Map<String, SimpliiAccountsWrapper.SimpliiAccount> {
        val request = Request.Builder()
            .url(ACCOUNTS_URL)
            .get()
            .addHeader("x-auth-token", token)
            .build()

        println()
        println("accounts request")
        println(request.toString())
        printHeaders(request.headers())

        println()
        println("accounts response")

        return client.newCall(request).execute().use(fun(response): Map<String, SimpliiAccountsWrapper.SimpliiAccount> {
            val json = response.body()?.string() ?: ""

            printHeaders(response.headers())
            println("body: $json")

            val adapter = moshi.adapter(SimpliiAccountsWrapper::class.java)
            return adapter.fromJson(json)
                ?.accounts
                ?.associate { account ->
                    account.number to account
                }
                ?: emptyMap()
        })
    }

    private fun getTransactions(
        client: OkHttpClient, moshi: Moshi, token: String, account: SimpliiAccountsWrapper.SimpliiAccount
    ): List<SimpliiTransactionWrapper.SimpliiTransaction> {
        val today = LocalDate.now()

        val url = HttpUrl.get(TRANSACTIONS_URL)
            .newBuilder()
            .addQueryParameter("accountId", account.id)
            .addQueryParameter("filterBy", "range")
            .addQueryParameter("lastFilterBy", "range")
            .addQueryParameter("fromDate", today.minusMonths(1L).format(DateTimeFormatter.ISO_LOCAL_DATE))
            .addQueryParameter("toDate", today.format(DateTimeFormatter.ISO_LOCAL_DATE))
            .addQueryParameter("sortAsc", "true")
            .addQueryParameter("sortByField", "date")

            // may not be needed
            .addQueryParameter("limit", "1000")
            .addQueryParameter("lowerLimitAmount", "")
            .addQueryParameter("upperLimitAmount", "")
            .addQueryParameter("offset", "0")
            .addQueryParameter("transactionType", "")

            .build()

        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("x-auth-token", token)
            .build()

        println()
        println("transactions request")
        println(request.toString())
        printHeaders(request.headers())

        println()
        println("transactions response")

        return client.newCall(request).execute().use(fun(response): List<SimpliiTransactionWrapper.SimpliiTransaction> {
            val json = response.body()?.string() ?: ""

            printHeaders(response.headers())
            println("body: $json")

            val adapter = moshi.adapter(SimpliiTransactionWrapper::class.java)
            return adapter.fromJson(json)
                ?.transactions
                ?: emptyList()
        })
    }

    private fun terminateSession(client: OkHttpClient, token: String): Pair<Int, String> {
        val request = Request.Builder()
            .url(AUTH_URL)
            .delete()
            .addHeader("x-auth-token", token)
            .build()

        println()
        println("terminate session request")
        println(request.toString())
        printHeaders(request.headers())

        println()
        println("terminate session response")

        return client.newCall(request).execute().use { response ->
            printHeaders(response.headers())
            response.code() to response.message()
        }
    }

    private fun printHeaders(headers: Headers) {
        for (name in headers.names()) {
            println("header '$name': ${headers[name]}")
        }
    }

    data class SimpliiAccountsWrapper(val accounts: List<SimpliiAccount> = emptyList()) {
        data class SimpliiAccount(val id: String, val number: String, val balance: SimpliiAccountBalance)
        data class SimpliiAccountBalance(val currency: String, val amount: String)
    }

    data class SimpliiTransactionWrapper(val transactions: List<SimpliiTransaction> = emptyList()) {
        data class SimpliiTransaction(
            val id: String,
            val accountId: String,
            val date: String,
            val descriptionLine1: String,
            val transactionDescription: String,
            val credit: String,
            val debit: String,
            val transactionType: TransactionType
        )
        enum class TransactionType {
            DEP, XFR, PAY, POS, INT, CHQ, CRE
        }
    }
}