package com.pissiphany.bany.plugin.equitable

import com.pissiphany.bany.plugin.BanyConfigurablePlugin
import com.pissiphany.bany.plugin.BanyPlugin
import com.pissiphany.bany.plugin.dataStructure.BanyPluginTransaction
import com.pissiphany.bany.plugin.dataStructure.BanyPluginBudgetAccountIds
import com.pissiphany.bany.plugin.dataStructure.BanyPluginAccountBalance
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.FormElement
import java.math.BigDecimal
import java.net.URL
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*

typealias Cookies = Map<String, String>

class EquitableLifePlugin(
    private val credentials: BanyPlugin.Credentials,
    domain: URL? = null
) : BanyConfigurablePlugin {
    private val root = domain ?: URL(EQUITABLE_ROOT)

    companion object {
        private const val EQUITABLE_ROOT = "https://client.equitable.ca"
        const val LOG_ON_URL = "/client/en/Account/LogOn"
        const val LOG_ON_ASK_SECURITY_URL = "/client/en/Account/LogOnAskSecurityQuestion"
        const val LOG_OUT_URL = "/client/en/Account/LogOut"
        const val POLICY_VALUES_URL = "/policy/en/Policy/Values"
        const val POLICY_INVESTMENTS_URL = "/policy/en/Policy/Investments"

        private const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.132 Safari/537.36"
        private const val PAYEE = "Equitable Life of Canada"

        const val ASPXAUTH = ".ASPXAUTH"
    }

    private enum class AccountType { INVESTMENT, INSURANCE, LIABILITY }

    private var sessionCookies: Cookies = emptyMap()

    override fun setup(): Boolean {
        val getLogOnResponse = getLogOnPage()
        val cookies = getLogOnResponse.cookies().toMutableMap()

        val postLogInResponse = postToLogIn(getLogOnResponse, cookies)
        cookies.putAll(postLogInResponse.cookies())

        val getLogOnAskSecurityQuestion = getLogOnAskSecurityQuestionPage(cookies)
        cookies.putAll(getLogOnAskSecurityQuestion.cookies())

        val postAnswerSecurityResponse = postToLogInAnswerSecurityAnswer(getLogOnAskSecurityQuestion, cookies)
        return completeLogin(postAnswerSecurityResponse)
    }

    private fun getLogOnPage() = Jsoup
        .connect(getUrl(LOG_ON_URL))
        .execute { code, msg -> "GET to LogOn failed: $code $msg" }

    private fun postToLogIn(getLogOnResponse: Connection.Response, cookies: Cookies): Connection.Response {
        val getLogOnDoc = getLogOnResponse.parse()
        val logOnForm = getLogOnDoc.getElementById("sign_in") as FormElement

        // populate username and password
        val usernameElement = logOnForm.getElementById("UserName")
        usernameElement.`val`(credentials.username)
        val passwordElement = logOnForm.getElementById("Password")
        passwordElement.`val`(credentials.password)

        // submit
        return logOnForm
            .submit()
            .followRedirects(false)
            .cookies(cookies)
            .execute(302) { code, msg -> "POST to LogIn failed: $code $msg" }
    }

    private fun getLogOnAskSecurityQuestionPage(cookies: Cookies) = Jsoup
        .connect(getUrl(LOG_ON_ASK_SECURITY_URL))
        .method(Connection.Method.GET)
        .cookies(cookies)
        .execute { code, msg -> "GET to LogOnAskSecurityQuestion failed: $code $msg" }

    private fun postToLogInAnswerSecurityAnswer(
        getLogOnAskSecurityQuestion: Connection.Response,
        cookies: Cookies
    ): Connection.Response {
        val logOnAskSecurityDoc = getLogOnAskSecurityQuestion.parse()
        val askSecurityForm = logOnAskSecurityDoc.getElementById("sign_in_question") as FormElement

        // populate security answer
        val answer = getSecurityAnswer(askSecurityForm.selectFirst("p > strong").text())
            ?: throw IllegalStateException("Unable to identify security question/answer")

        val answerElement = askSecurityForm.getElementById("Answer")
        answerElement.`val`(answer)

        // submit
        return askSecurityForm
            .submit()
            .followRedirects(false)
            .cookies(cookies)
            .execute(302) { code, msg -> "POST to LogInAnswerSecurityQuestion failed: $code $msg" }
    }

    private fun getSecurityAnswer(question: String): String? {
        return credentials.data.mapKeys { (key, _) -> key.toLowerCase() }[question.toLowerCase()]
    }

    private fun completeLogin(postAnswerSecurityResponse: Connection.Response): Boolean {
        if (!postAnswerSecurityResponse.hasCookie(ASPXAUTH)) return false
        sessionCookies = postAnswerSecurityResponse.cookies().toMap()
        return true
    }

    override fun tearDown() {
        super.tearDown()

        checkSession()

        Jsoup
            .connect(getUrl(LOG_OUT_URL))
            .method(Connection.Method.GET)
            .cookies(sessionCookies)
            .followRedirects(false)
            .execute(302) { code, msg -> "GET to LogOut failed: $code $msg" }

        sessionCookies = emptyMap()
    }

    private fun checkSession() = check(sessionCookies.containsKey(ASPXAUTH)) { "Missing $ASPXAUTH session token!" }

    override fun getBanyPluginBudgetAccountIds(): List<BanyPluginBudgetAccountIds> {
        return credentials.connections
            .map { BanyPluginBudgetAccountIds(
                ynabAccountId = it.ynabAccountId,
                ynabBudgetId = it.ynabBudgetId
            ) }
    }

    override fun getNewBanyPluginTransactionsSince(
        budgetAccountIds: BanyPluginBudgetAccountIds,
        date: LocalDate?
    ): List<BanyPluginTransaction> {
        checkSession()

        val connection = getConnection(budgetAccountIds)
        return when (val type = connection.getAccountType()) {
            AccountType.INSURANCE -> getInsuranceTransaction(connection)
            AccountType.INVESTMENT -> getInvestmentTransaction(connection)
            AccountType.LIABILITY -> getInsuranceLiabilityTransaction(connection)
            else -> throw IllegalArgumentException("$type unsupported!")
        }
    }

    private fun getInsuranceTransaction(connection: BanyPlugin.Connection): List<BanyPluginTransaction> {
        val details = getInsuranceDetails(connection)

        return listOf(
            BanyPluginAccountBalance(
                date = OffsetDateTime.now(ZoneOffset.UTC),
                payee = PAYEE,
                amount = details.loadAvailable + details.loanBalance
            )
        )
    }

    private fun getInsuranceDetails(connection: BanyPlugin.Connection): InsuranceDetails {
        val getAccountDetailsResponse = Jsoup
            .connect(getUrl(POLICY_VALUES_URL, connection.thirdPartyAccountId))
            .method(Connection.Method.GET)
            .cookies(sessionCookies)
            .followRedirects(false)
            .appendTimestamp()
            .execute { code, msg -> "GET to $POLICY_VALUES_URL failed: $code $msg" }

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

        return InsuranceDetails(
            loadAvailable = loanAvailable,
            loanBalance = loanBalance
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

    private fun getInsuranceLiabilityTransaction(connection: BanyPlugin.Connection): List<BanyPluginTransaction> {
        val totalLiabilities = credentials.connections
            .filter { it != connection && it.getAccountType() == AccountType.INSURANCE }
            .map(::getInsuranceDetails)
            .fold(BigDecimal(0)) { total, account -> total - account.loanBalance }

        return listOf(
            BanyPluginAccountBalance(
                date = OffsetDateTime.now(ZoneOffset.UTC),
                payee = PAYEE,
                amount = totalLiabilities
            )
        )
    }

    private fun getInvestmentTransaction(connection: BanyPlugin.Connection): List<BanyPluginTransaction> {
        val details = getInvestmentDetails(connection)

        return listOf(
            BanyPluginAccountBalance(
                date = OffsetDateTime.now(ZoneOffset.UTC),
                payee = PAYEE,
                amount = details.marketValue
            )
        )
    }

    private fun getInvestmentDetails(connection: BanyPlugin.Connection): InvestmentDetails {
        val getAccountDetailsResponse = Jsoup
            .connect(getUrl(POLICY_INVESTMENTS_URL, connection.thirdPartyAccountId))
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

        return InvestmentDetails(
            totalDeposits = totalDeposits,
            totalWithdrawals = totalWithdrawals,
            netDeposits = netDeposits,
            marketValue = marketValue
        )
    }

    private fun getUrl(vararg parts: String) = URL(root, parts.joinToString("/")).toString()

    private fun Connection.appendTimestamp() = this.data("_", Date().time.toString())

    private fun Connection.execute(expected: Int = 200, err: (code: Int, msg: String) -> String): Connection.Response {
       return this
           .userAgent(USER_AGENT)
           .execute()
           .also { res ->
               check(res.statusCode() == expected) { err(res.statusCode(), res.statusMessage()) }
           }
    }

    private fun getConnection(budgetAccountIds: BanyPluginBudgetAccountIds): BanyPlugin.Connection {
        return credentials.connections.single { con ->
            budgetAccountIds.run {
                ynabBudgetId == con.ynabBudgetId && ynabAccountId == con.ynabAccountId
            }
        }
    }

    private fun BanyPlugin.Connection.getAccountType(): AccountType {
        val type = data["accountType"] ?: return AccountType.INSURANCE
        return checkNotNull(AccountType.values().singleOrNull { it.name.equals(type, ignoreCase = true) }) {
            "Unable to find account type matching '$type'"
        }
    }

    private data class InsuranceDetails(
        val loadAvailable: BigDecimal,
        val loanBalance: BigDecimal
    )

    private data class InvestmentDetails(
        val totalDeposits: BigDecimal,
        val totalWithdrawals: BigDecimal,
        val netDeposits: BigDecimal,
        val marketValue: BigDecimal,
    )

    inner class TestBackdoor {
        var sessionCookies: Cookies
            get() = this@EquitableLifePlugin.sessionCookies
            set(value) {
                this@EquitableLifePlugin.sessionCookies = value
            }
    }
}