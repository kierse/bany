package com.pissiphany.bany.plugin.cibc

import com.pissiphany.bany.plugin.BanyPlugin
import com.pissiphany.bany.plugin.dataStructure.BanyPluginTransaction
import com.pissiphany.bany.plugin.cibc.dataStructure.AuthRequest
import com.pissiphany.bany.plugin.cibc.dataStructure.CibcAccountsWrapper
import com.pissiphany.bany.plugin.cibc.dataStructure.CibcTransactionWrapper
import com.pissiphany.bany.plugin.cibc.environment.Environment
import com.pissiphany.bany.plugin.cibc.mapper.CibcTransactionMapper
import com.pissiphany.bany.plugin.dataStructure.YnabBudgetAccountIds
import com.squareup.moshi.Moshi
import okhttp3.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private const val ACCEPT_HEADER = "accept"
private const val ACCEPT_LANGUAGE_HEADER = "accept-language"
private const val USER_AGENT_HEADER = "user-agent"
private const val USER_AGENT_HEADER_VALUE = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.119 Safari/537.36"
private const val X_AUTH_TOKEN_HEADER = "x-auth-token"

typealias AccountId = String
typealias XAuthToken = String

// TODO create a factory extension point interface
// TODO instances of this interface will have one method that instantiates and returns
// TODO ThirdPartyTransactionService (or similar) instances. This will allow me to
// TODO create transaction gateways that accept all dependencies via the constructor
// TOOD making them far more testable
class CibcTransactionService(
    private val credentials: BanyPlugin.Credentials,
    private val env: Environment,
    private val moshi: Moshi,
    private val client: OkHttpClient,
    private val mapper: CibcTransactionMapper
) : BanyPlugin {

    private var token: String = ""
    private var accounts: Map<AccountId, CibcAccountsWrapper.CibcAccount> = emptyMap()

    override val name = env.name

    override fun setup(credentials: BanyPlugin.Credentials): Boolean {
        seedCookieJar("foo") // TODO load static payload from disk
        seedCookieJar("bar")

        token = authenticate() ?: return false
        accounts = fetchAccounts()

        return accounts.isNotEmpty()
    }

    private fun seedCookieJar(payload: String) {
        val mediaType = MediaType.parse("text/plain;charset=UTF-8")
        val body = RequestBody.create(mediaType, payload)

        val request = Request.Builder()
            .url(env.staticUrl)
            .post(body)
            .addHeader(ACCEPT_HEADER, "*/*")
            .addHeader(ACCEPT_LANGUAGE_HEADER, "en-US,en;q=0.9")
            .addHeader(USER_AGENT_HEADER, USER_AGENT_HEADER_VALUE)
            .build()

        client
            .newCall(request)
            .execute()
            .use { response ->
                if (response.code() != 201) throw UnexpectedResponseException("expected 201, received ${response.code()}")
            }
    }

    private fun authenticate(): XAuthToken? {
        val authAdapter = moshi.adapter(AuthRequest::class.java)
        val json = authAdapter.toJson(
            AuthRequest(
                card = AuthRequest.Card(
                    value = credentials.username,
                    description = "",
                    encrypted = false,
                    encrypt = true
                ),
                password = credentials.password
            )
        )

        val contentType = "application/vnd.api+json"
        val mediaType = MediaType.parse(contentType)
        val body = RequestBody.create(mediaType, json)

        val request = Request.Builder()
            .url(env.authUrl)
            .post(body)
            .addHeader(ACCEPT_HEADER, contentType)
            .addHeader("accept-encoding", "gzip, deflate, br")
            .addHeader(ACCEPT_LANGUAGE_HEADER, "en")
            .addHeader("brand", env.brand)
            .addHeader("client-type", "default_web")
            .addHeader("content-type", contentType)
            .addHeader("dnt", "1")
            .addHeader("host", env.host)
            .addHeader("origin", env.baseUrl)
            .addHeader("referer", env.refererUrl)
            .addHeader(USER_AGENT_HEADER, USER_AGENT_HEADER_VALUE)
            .addHeader("www-authenticate", "CardAndPassword")
            .addHeader(X_AUTH_TOKEN_HEADER, "")
            .addHeader("x-requested-with", "XMLHttpRequest")
            .build()

        return client
            .newCall(request)
            .execute()
            .use { response ->
                response.header(X_AUTH_TOKEN_HEADER)
            }
    }

    private fun fetchAccounts(): Map<AccountId, CibcAccountsWrapper.CibcAccount> {
        val request = Request.Builder()
            .url(env.accountsUrl)
            .get()
            .addHeader(X_AUTH_TOKEN_HEADER, token)
            .build()

        return client
            .newCall(request)
            .execute()
            .use(
                fun(response): Map<AccountId, CibcAccountsWrapper.CibcAccount> {
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
            .addHeader(X_AUTH_TOKEN_HEADER, token)
            .build()

        client
            .newCall(request)
            .execute()

        token = ""
    }

    override fun getYnabBudgetAccountIds(): List<YnabBudgetAccountIds> {
        return credentials.connections
            .map { YnabBudgetAccountIds(
                ynabBudgetId = it.ynabBudgetId, ynabAccountId = it.ynabAccountId
            ) }
    }

    override fun getNewBanyPluginTransactionsSince(
        ynabBudgetAccountIds: YnabBudgetAccountIds, date: LocalDate?
    ): List<BanyPluginTransaction> {
        val accountId = getAccountId(ynabBudgetAccountIds)
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
            val now = LocalDate.now()
            builder = builder
                .addQueryParameter("fromDate", date.format(DateTimeFormatter.ISO_LOCAL_DATE))
                .addQueryParameter("toDate", now.format(DateTimeFormatter.ISO_LOCAL_DATE))
        }

        val request = Request.Builder()
            .url(builder.build())
            .get()
            .addHeader(X_AUTH_TOKEN_HEADER, token)
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

    private fun getAccountId(ynabBudgetAccountIds: YnabBudgetAccountIds): AccountId {
        for (connection in credentials.connections) {
            if (connection.ynabBudgetId == ynabBudgetAccountIds.ynabBudgetId
                && connection.ynabAccountId == ynabBudgetAccountIds.ynabAccountId) {
                return connection.thirdPartyAccountId
            }
        }

        throw UnknownYnabBudgetAndAccountException("no credentials found for: $ynabBudgetAccountIds")
    }

    class UnexpectedResponseException(message: String, cause: Throwable? = null) : Throwable(message, cause)
    class UnknownYnabBudgetAndAccountException(message: String) : Throwable(message)
}

