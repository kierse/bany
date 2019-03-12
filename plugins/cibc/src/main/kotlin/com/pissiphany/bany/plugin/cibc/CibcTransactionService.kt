package com.pissiphany.bany.plugin.cibc

import com.pissiphany.bany.plugin.BanyPlugin
import com.pissiphany.bany.plugin.dataStructure.BanyPluginTransaction
import com.pissiphany.bany.plugin.cibc.dataStructure.AuthRequest
import com.pissiphany.bany.plugin.cibc.dataStructure.CibcAccountsWrapper
import com.pissiphany.bany.plugin.cibc.dataStructure.CibcTransactionWrapper
import com.pissiphany.bany.plugin.cibc.environment.Environment
import com.pissiphany.bany.plugin.cibc.mapper.CibcTransactionMapper
import com.squareup.moshi.Moshi
import okhttp3.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// TODO create a factory extension point interface
// TODO instances of this interface will have one method that instantiates and returns
// TODO ThirdPartyTransactionService (or similar) instances. This will allow me to
// TODO create transaction gateways that accept all dependencies via the constructor
// TOOD making them far more testable
class CibcTransactionService(
    private val configuration: BanyPlugin.Configuration,
    private val env: Environment,
    private val moshi: Moshi,
    private val client: OkHttpClient,
    private val mapper: CibcTransactionMapper
) : BanyPlugin {
    class UnexpectedResponseException(message: String, cause: Throwable? = null) : Throwable(message, cause)

    private val userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.119 Safari/537.36"
    private var token: String = ""

    override val name: String
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override fun setup(configuration: BanyPlugin.Configuration): Boolean {
        seedCookieJar("foo") // TODO load static payload from disk
        seedCookieJar("bar")
        authenticate()

        return token.isNotBlank()
    }

    private fun seedCookieJar(payload: String) {
        val mediaType = MediaType.parse("text/plain;charset=UTF-8")
        val body = RequestBody.create(mediaType, payload)

        val request = Request.Builder()
            .url(env.staticUrl)
            .post(body)
            .addHeader("accept", "*/*")
            .addHeader("accept-language", "en-US,en;q=0.9")
//            .addHeader("content-type", "text/plain;charset=UTF-8")
            .addHeader("user-agent", userAgent)
            .build()

        client
            .newCall(request)
            .execute()
            .use { response ->
                if (response.code() != 201) throw UnexpectedResponseException("expected 201, received ${response.code()}")
            }
    }

    private fun authenticate() {
        val authAdapter = moshi.adapter(AuthRequest::class.java)
        val json = authAdapter.toJson(
            AuthRequest(
                card = AuthRequest.Card(
                    value = configuration.username,
                    description = "",
                    encrypted = false,
                    encrypt = true
                ),
                password = configuration.password
            )
        )

        val contentType = "application/vnd.api+json"
        val mediaType = MediaType.parse(contentType)
        val body = RequestBody.create(mediaType, json)

        val request = Request.Builder()
            .url(env.authUrl)
            .post(body)
            .addHeader("accept", contentType)
            .addHeader("accept-encoding", "gzip, deflate, br")
            .addHeader("accept-language", "en")
            .addHeader("brand", env.brand)
            .addHeader("client-type", "default_web")
            .addHeader("content-type", contentType)
            .addHeader("dnt", "1")
            .addHeader("host", env.host)
            .addHeader("origin", env.baseUrl)
            .addHeader("referer", env.refererUrl)
            .addHeader("user-agent", userAgent)
            .addHeader("www-authenticate", "CardAndPassword")
            .addHeader("x-auth-token", "")
            .addHeader("x-requested-with", "XMLHttpRequest")
            .build()

        token = client
            .newCall(request)
            .execute()
            .use { response ->
                response.header("x-auth-token") ?: ""
            }
    }

    private fun getAccounts(): Map<AccountId, CibcAccountsWrapper.CibcAccount> {
        val request = Request.Builder()
            .url(env.accountsUrl)
            .get()
            .addHeader("x-auth-token", token)
            .build()

        return client
            .newCall(request)
            .execute()
            .use(
                fun(response): Map<String, CibcAccountsWrapper.CibcAccount> {
                    val json = response.body()?.string() ?: ""

                    val adapter = moshi.adapter(CibcAccountsWrapper::class.java)
                    return adapter.fromJson(json)
                        ?.accounts
                        ?.associate { account ->
                            account.number to account
                        }
                        ?: emptyMap()
                }
            )
    }

    override fun tearDown() {
        val request = Request.Builder()
            .url(env.authUrl)
            .delete()
            .addHeader("x-auth-token", token)
            .build()

        client
            .newCall(request)
            .execute()

        token = ""
    }

    override fun getYnabAccountId(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getNewTransactionsSince(accountId: AccountId, date: LocalDate?): List<BanyPluginTransaction> {
        val now = LocalDate.now()

        var builder = HttpUrl.get(env.transactionsUrl).newBuilder()
            .addQueryParameter("accountId", accountId)
            .addQueryParameter("filterBy", "range")
            .addQueryParameter("lastFilterBy", "range")
            .addQueryParameter("limit", "1000")
            .addQueryParameter("lowerLimitAmount", "")
            .addQueryParameter("offset", "0")
            .addQueryParameter("sortAsc", "true")
            .addQueryParameter("sortByField", "date")
            .addQueryParameter("transactionType", "")
            .addQueryParameter("upperLimitAmount", "")

        if (date != null) {
            builder = builder
                .addQueryParameter("fromDate", date.format(DateTimeFormatter.ISO_LOCAL_DATE))
                .addQueryParameter("toDate", now.format(DateTimeFormatter.ISO_LOCAL_DATE))
        }

        val request = Request.Builder()
            .url(builder.build())
            .get()
            .addHeader("x-auth-token", token)
            .build()

        return client
            .newCall(request)
            .execute()
            .use(
                fun(response): List<BanyPluginTransaction> {
                    val json = response.body()?.string() ?: return emptyList()

                    val adapter = moshi.adapter(CibcTransactionWrapper::class.java)
                    return adapter.fromJson(json)
                        ?.transactions
                        ?.map(mapper::toBanyPluginTransaction)
                        ?: emptyList()
                }
            )
    }
}

typealias AccountId = String
