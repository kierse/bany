package com.pissiphany.bany.plugin.equitable.client

import com.pissiphany.bany.plugin.BanyPlugin
import com.pissiphany.bany.plugin.equitable.client.EquitableClient.EquitableClientSession
import okhttp3.HttpUrl
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.math.BigDecimal
import java.net.URL
import java.util.*

internal const val LOG_OUT_URL = "/client/en/Account/LogOut"
internal const val POLICY_VALUES_URL = "/policy/en/Policy/Values"
internal const val POLICY_INVESTMENTS_URL = "/policy/en/Policy/Investments"
internal const val ASPXAUTH = ".ASPXAUTH"

class EquitableClientSessionImpl(
    private val root: URL,
    private var sessionCookies: Map<String, String>
) : EquitableClientSession {
    constructor(
        root: HttpUrl,
        sessionCookies: Cookies
    ): this(root.toUrl(), emptyMap())

    override suspend fun terminateSession() {
        checkSession()

        Jsoup
            .connect(root.addToPath(LOG_OUT_URL))
            .method(Connection.Method.GET)
            .cookies(sessionCookies)
            .followRedirects(false)
            .execute(302) { code, msg -> "GET to LogOut failed: $code $msg" }

        sessionCookies = emptyMap()
    }

    override fun isValid() = sessionCookies.containsKey(ASPXAUTH)
    override fun checkSession() = check(isValid()) { "Missing $ASPXAUTH session token!" }

    override suspend fun getInsuranceDetails(connection: BanyPlugin.Connection): EquitableClientSession.InsuranceDetails {
        val getAccountDetailsResponse = fetchInsuranceData(connection, POLICY_VALUES_URL)
        val getAccountDetailsDoc = getAccountDetailsResponse.parse()
        val rows = getAccountDetailsDoc.select("div.details_row")

        val loanBalanceRowElem = rows.first {
            it.selectFirst("div.grid_4.detail_label > p:contains(loan balance)") != null
        }
        val loanBalance = checkNotNull(loanBalanceRowElem.getDollarValue()) {
            "Unable to identify current loan balance!"
        }

        val loanAvailableRowElem = rows.asReversed().first {
            it.selectFirst("div.grid_4.detail_label > p:contains(loan available)") != null
        }
        val loanAvailable = checkNotNull(loanAvailableRowElem.getDollarValue()) {
            "Unable to identify available loan amount!"
        }

        return EquitableClientSession.InsuranceDetails(
            loanAvailable = loanAvailable,
            loanBalance = loanBalance
        )
    }

    override suspend fun getInvestmentDetails(connection: BanyPlugin.Connection): EquitableClientSession.InvestmentDetails {
        val getAccountDetailsResponse = Jsoup
            .connect(root.addToPath(POLICY_INVESTMENTS_URL, connection.thirdPartyAccountId))
            .method(Connection.Method.GET)
            .cookies(sessionCookies)
            .followRedirects(false)
            .appendTimestamp()
            .execute { code, msg -> "GET to $POLICY_INVESTMENTS_URL failed: $code $msg" }

        val getAccountDetailsDoc = getAccountDetailsResponse.parse()
        val table = getAccountDetailsDoc.selectFirst(".tbl_total_investments")
        val headings = table.select("thead > tr > th")
        val values = table.select("tbody > tr > td")

        val headingToValue = mutableMapOf<String, String>()
        headings.forEachIndexed { i, element ->
            headingToValue[element.text().toLowerCase()] = values[i].text()
        }

        val totalDeposits = checkNotNull(headingToValue["total deposits"].getDollarValue()) {
            "Unable to identify total deposits!"
        }
        val totalWithdrawals = checkNotNull(headingToValue["total withdrawals"].getDollarValue()) {
            "Unable to identify total withdrawals!"
        }
        val netDeposits = checkNotNull(headingToValue["net deposits"].getDollarValue()) {
            "Unable to identify net deposits!"
        }
        val marketValue = checkNotNull(headingToValue["total market value"].getDollarValue()) {
            "Unable to identify market value!"
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
            char.toInt().let {
                // a .. z      ||    .
                (it in 48..57) || it == 46
            }
        }
        ?.takeIf(String::isNotBlank)
        ?.let(::BigDecimal)

    private fun Connection.appendTimestamp() = this.data("_", Date().time.toString())

    private fun fetchInsuranceData(connection: BanyPlugin.Connection, relativeUrl: String): Connection.Response {
        return Jsoup
            .connect(root.addToPath(relativeUrl, connection.thirdPartyAccountId))
            .method(Connection.Method.GET)
            .cookies(sessionCookies)
            .followRedirects(false)
            .appendTimestamp()
            .execute { code, msg -> "GET to $relativeUrl failed: $code $msg" }
    }
}