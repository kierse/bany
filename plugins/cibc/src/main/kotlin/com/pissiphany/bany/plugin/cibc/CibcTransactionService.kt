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

private const val BRAND_HEADER = "brand"
private const val CONTENT_TYPE_HEADER_VALUE = "application/json"
internal const val X_AUTH_TOKEN_HEADER = "x-auth-token"

typealias AccountId = String
typealias XAuthToken = String

internal class CibcTransactionService(
    private val credentials: BanyPlugin.Credentials,
    private val env: Environment,
    private val moshi: Moshi,
    private val client: (request: Request) -> Response,
    private val mapper: CibcTransactionMapper
) : BanyPlugin {
    private var token: String = ""
    private var accounts: Map<AccountId, CibcAccountsWrapper.CibcAccount> = emptyMap()

    override fun setup(): Boolean {
        if (!fetchAppConfig()) return false

        token = authenticate() ?: return false
        accounts = fetchAccounts()

        return accounts.isNotEmpty()
    }

    private fun fetchAppConfig(): Boolean {
        val request = Request.Builder()
            .url(env.appConfigUrl)
            .get()
            .addHeader(BRAND_HEADER, env.brand)
            .build()

        client(request).use { response ->
            if (response.code() != 200) {
                // TODO proceed but log the error
                return false
            }
        }

        return true
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

        val mediaType = MediaType.parse(CONTENT_TYPE_HEADER_VALUE)
        val body = RequestBody.create(mediaType, json)

        val request = Request.Builder()
            .url(env.authUrl)
            .post(body)
            .addHeader("www-authenticate", "CardAndPassword")
            .addRequiredHeaders()
            .build()

        return client(request)
            .use { response ->
                response.header(X_AUTH_TOKEN_HEADER)
            }
    }

    private fun fetchAccounts(): Map<AccountId, CibcAccountsWrapper.CibcAccount> {
        val request = Request.Builder()
            .url(env.accountsUrl)
            .get()
            .addRequiredHeaders()
            .build()

        return client(request)
            .use(
                fun(response): Map<AccountId, CibcAccountsWrapper.CibcAccount> {
                    if (response.code() != 200) {
                        // TODO log / throw??
                        return emptyMap()
                    }

                    // TODO log
                    val json = response.body()?.string() ?: return emptyMap()

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
        if (token.isBlank()) return

        val request = Request.Builder()
            .url(env.authUrl)
            .delete()
            .addRequiredHeaders()
            .build()

        client(request)
            .close() // TODO log response code

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
        val url = buildTransactionsRequestUrl(ynabBudgetAccountIds, date)
        val request = Request.Builder()
            .url(url)
            .get()
            .addRequiredHeaders()
            .build()

        return client(request)
            .use(
                fun(response): List<BanyPluginTransaction> {
                    if (response.code() == 422) { // no results
                        return emptyList()
                    } else if (response.code() != 200) {
                        // TODO log / throw error
                        return emptyList()
                    }

                    val json = response.body()?.string() ?: return emptyList()

                    val adapter = moshi.adapter(CibcTransactionWrapper::class.java)
                    return adapter.fromJson(json)
                        ?.transactions
                        ?.map(mapper::toBanyPluginTransaction)
                        ?: emptyList()
                }
            )
    }

    private fun buildTransactionsRequestUrl(ynabBudgetAccountIds: YnabBudgetAccountIds, date: LocalDate?): HttpUrl {
        val accountId = getInternalAccountId(ynabBudgetAccountIds)
        val builder = HttpUrl
            .get(env.transactionsUrl)
            .newBuilder()
            .addQueryParameter("accountId", accountId)
            .addQueryParameter("filterBy", "range")
            .addQueryParameter("lastFilterBy", "range")
            .addQueryParameter("limit", "1000")
            .addQueryParameter("offset", "0")
            .addQueryParameter("sortAsc", "true")
            .addQueryParameter("sortByField", "date")

        if (date != null) {
            val now = LocalDate.now()
            builder.addQueryParameter("fromDate", date.format(DateTimeFormatter.ISO_LOCAL_DATE))
                   .addQueryParameter("toDate", now.format(DateTimeFormatter.ISO_LOCAL_DATE))
        }

        return builder.build()
    }

    private fun getInternalAccountId(ynabBudgetAccountIds: YnabBudgetAccountIds): AccountId {
        for (connection in credentials.connections) {
            if (connection.ynabBudgetId == ynabBudgetAccountIds.ynabBudgetId
                && connection.ynabAccountId == ynabBudgetAccountIds.ynabAccountId) {
                if (connection.thirdPartyAccountId in accounts) {
                    return accounts.getValue(connection.thirdPartyAccountId).id
                }
            }
        }

        throw IllegalArgumentException("no credentials found for: $ynabBudgetAccountIds")
    }

    private fun Request.Builder.addRequiredHeaders() =
        this.addHeader("accept", CONTENT_TYPE_HEADER_VALUE)
            .addHeader("accept-language", "en")
            .addHeader(BRAND_HEADER, env.brand)
            .addHeader("content-type", CONTENT_TYPE_HEADER_VALUE)
            .also { if (token.isNotBlank()) addHeader(X_AUTH_TOKEN_HEADER, token) }
}

