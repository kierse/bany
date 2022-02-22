package com.pissiphany.bany.plugin.equitable.client

import com.pissiphany.bany.plugin.BanyPlugin
import com.pissiphany.bany.plugin.equitable.client.EquitableClient.EquitableClientSession
import com.pissiphany.bany.shared.logger
import okhttp3.HttpUrl
import okhttp3.Request
import org.jsoup.nodes.Element
import java.math.BigDecimal
import java.util.*

internal const val LOG_OUT_URL = "client/en/Account/LogOut"
internal const val POLICY_VALUES_URL = "policy/en/Policy/Values"
internal const val POLICY_INVESTMENTS_URL = "policy/en/Policy/Investments"
internal const val ASPXAUTH = ".ASPXAUTH"

class EquitableClientSessionImpl(
    private val clientWrapper: OkHttpWrapper,
    private val root: HttpUrl,
    private var sessionCookies: Cookies
) : EquitableClientSession {
    private val logger by logger()

    override suspend fun terminateSession() {
        checkSession()

        val getLogOutUrl = root.newBuilder()
            .addPathSegments(LOG_OUT_URL)
            .build()
        val getLogOutRequest = Request.Builder()
            .get()
            .url(getLogOutUrl)
            .cookies(sessionCookies)
            .build()

        clientWrapper.fetchRedirectCookies(getLogOutRequest)

        sessionCookies = emptyList()
    }

    override fun isValid() = sessionCookies.any { cookie -> cookie.startsWith(ASPXAUTH) }
    override fun checkSession() = check(isValid()) { "Missing $ASPXAUTH session token!" }

    override suspend fun getInsuranceDetails(connection: BanyPlugin.Connection): EquitableClientSession.InsuranceDetails? {
        val getPolicyValuesUrl = root.newBuilder()
            .addPathSegments(POLICY_VALUES_URL)
            .addPathSegment(connection.thirdPartyAccountId)
            .appendTimestamp()
            .build()
        val getPolicyValuesRequest = Request.Builder()
            .get()
            .url(getPolicyValuesUrl)
            .cookies(sessionCookies)
            .build()
        val getPolicyValuesResponseData = clientWrapper.fetchAndProcess(getPolicyValuesRequest)
        if (getPolicyValuesResponseData == null) {
            logger.error("Failed to fetch policy values data!")
            return null
        }

        val rows = getPolicyValuesResponseData.document.select("div.details_row")
        if (rows.isEmpty()) {
            logger.error("Unable to find insurance details!")
            return null
        }

        val loanBalanceRowElem = rows.firstOrNull {
            it.selectFirst("div.grid_4.detail_label > p:contains(loan balance)") != null
        }
        val loanBalance = loanBalanceRowElem?.getDollarValue()
        if (loanBalance == null) {
            logger.error("Unable to identify current loan balance!")
            return null
        }

        val loanAvailableRowElem = rows.asReversed().firstOrNull {
            it.selectFirst("div.grid_4.detail_label > p:contains(loan available)") != null
        }
        val loanAvailable = loanAvailableRowElem?.getDollarValue()
        if (loanAvailable == null) {
            logger.error("Unable to identify available loan amount!")
            return null
        }

        return EquitableClientSession.InsuranceDetails(
            loanAvailable = loanAvailable,
            loanBalance = loanBalance
        )
    }

    override suspend fun getInvestmentDetails(connection: BanyPlugin.Connection): EquitableClientSession.InvestmentDetails? {
        val getPolicyInvestmentsUrl = root.newBuilder()
            .addPathSegments(POLICY_INVESTMENTS_URL)
            .addPathSegment(connection.thirdPartyAccountId)
            .appendTimestamp()
            .build()
        val getPolicyInvestmentsRequest = Request.Builder()
            .get()
            .url(getPolicyInvestmentsUrl)
            .cookies(sessionCookies)
            .build()
        val getPolicyInvestmentsResponseData = clientWrapper.fetchAndProcess(getPolicyInvestmentsRequest)
        if (getPolicyInvestmentsResponseData == null) {
            logger.error("Failed to fetch policy investments data!")
            return null
        }

        val table = getPolicyInvestmentsResponseData.document.selectFirst(".tbl_total_investments")
        if (table == null) {
            logger.error("Unable to find investments table!")
            return null
        }

        val headings = table.select("thead > tr > th")
        val values = table.select("tbody > tr > td")

        val headingToValue = mutableMapOf<String, String>()
        headings.forEachIndexed { i, element ->
            headingToValue[element.text().lowercase(Locale.getDefault())] = values[i].text()
        }

        val totalDeposits = headingToValue["total deposits"].getDollarValue()
        if (totalDeposits == null) {
            logger.error("Unable to identify total deposits!")
            return null
        }
        val totalWithdrawals = headingToValue["total withdrawals"].getDollarValue()
        if (totalWithdrawals == null) {
            logger.error("Unable to identify total withdrawals!")
            return null
        }
        val netDeposits = headingToValue["net deposits"].getDollarValue()
        if (netDeposits == null) {
            logger.error("Unable to identify net deposits!")
            return null
        }
        val marketValue = headingToValue["total market value"].getDollarValue()
        if (marketValue == null) {
            logger.error("Unable to identify market value!")
            return null
        }

        return EquitableClientSession.InvestmentDetails(
            totalDeposits = totalDeposits,
            totalWithdrawals = totalWithdrawals,
            netDeposits = netDeposits,
            marketValue = marketValue
        )
    }

    private fun Element.getDollarValue() = select("div.grid_8.omega > p")
        .text()
        .getDollarValue()

    private fun String?.getDollarValue() = this
        ?.filter { char ->
            char.code.let {
                // a .. z || .
                (it in 48..57) || it == 46
            }
        }
        ?.takeIf(String::isNotBlank)
        ?.let(::BigDecimal)

    private fun HttpUrl.Builder.appendTimestamp() = addQueryParameter("_", Date().time.toString())
}