package com.pissiphany.bany.plugin.equitable.client

import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.FormElement
import java.net.URL

typealias Cookies = Map<String, String>

private const val EQUITABLE_ROOT = "https://client.equitable.ca"
internal const val LOG_ON_URL = "/client/en/Account/LogOn"
internal const val LOG_ON_ASK_SECURITY_URL = "/client/en/Account/LogOnAskSecurityQuestion"

class EquitableClientImpl(domain: URL? = null) : EquitableClient {
    private val root = domain ?: URL(EQUITABLE_ROOT)

    override fun createSession(
        username: String,
        password: String,
        securityQuestions: Map<String, String>
    ): EquitableClient.EquitableClientSession {
        val getLogOnResponse = getLogOnPage()
        val cookies = getLogOnResponse.cookies().toMutableMap()

        val postLogInResponse = postToLogIn(getLogOnResponse, cookies, username, password)
        cookies.putAll(postLogInResponse.cookies())

        val getLogOnAskSecurityQuestion = getLogOnAskSecurityQuestionPage(cookies)
        cookies.putAll(getLogOnAskSecurityQuestion.cookies())

        val postAnswerSecurityResponse = postToLogInAnswerSecurityAnswer(
            getLogOnAskSecurityQuestion, cookies, securityQuestions
        )
        cookies.clear()
        cookies.putAll(postAnswerSecurityResponse.cookies())

        checkNotNull(postAnswerSecurityResponse.cookie(ASPXAUTH)) { "Session token missing!" }

        return EquitableClientSessionImpl(root, cookies)
    }

    private fun getLogOnPage() = Jsoup
        .connect(root.addToPath(LOG_ON_URL))
        .execute { code, msg -> "GET to LogOn failed: $code $msg" }

    private fun postToLogIn(
        getLogOnResponse: Connection.Response,
        cookies: Cookies,
        username: String,
        password: String
    ): Connection.Response {
        val getLogOnDoc = getLogOnResponse.parse()
        val logOnForm = getLogOnDoc.getElementById("sign_in") as FormElement

        // populate username and password
        val usernameElement = logOnForm.getElementById("UserName")
        usernameElement.`val`(username)
        val passwordElement = logOnForm.getElementById("Password")
        passwordElement.`val`(password)

        // submit
        return logOnForm
            .submit()
            .followRedirects(false)
            .cookies(cookies)
            .execute(302) { code, msg -> "POST to LogIn failed: $code $msg" }
    }

    private fun getLogOnAskSecurityQuestionPage(cookies: Cookies) = Jsoup
        .connect(root.addToPath(LOG_ON_ASK_SECURITY_URL))
        .method(Connection.Method.GET)
        .cookies(cookies)
        .execute { code, msg -> "GET to LogOnAskSecurityQuestion failed: $code $msg" }

    private fun postToLogInAnswerSecurityAnswer(
        getLogOnAskSecurityQuestion: Connection.Response,
        cookies: Cookies,
        securityQuestions: Map<String, String>
    ): Connection.Response {
        val logOnAskSecurityDoc = getLogOnAskSecurityQuestion.parse()
        val askSecurityForm = logOnAskSecurityDoc.getElementById("sign_in_question") as FormElement

        // populate security answer
        val answer = securityQuestions.getSecurityAnswer(askSecurityForm.selectFirst("p > strong").text())
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

    private fun Map<String, String>.getSecurityAnswer(question: String): String? {
        return mapKeys { (key, _) -> key.toLowerCase() }[question.toLowerCase()]
    }
}