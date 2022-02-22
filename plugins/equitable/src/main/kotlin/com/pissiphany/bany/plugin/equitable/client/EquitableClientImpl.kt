package com.pissiphany.bany.plugin.equitable.client

import com.pissiphany.bany.shared.logger
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import org.jsoup.nodes.Document
import org.jsoup.nodes.FormElement
import java.util.*

typealias Cookies = List<String>

internal const val LOG_ON_URL = "client/en/Account/LogOn"
internal const val LOG_ON_ASK_SECURITY_URL = "client/en/Account/LogOnAskSecurityQuestion"

class EquitableClientImpl(
    private val clientWrapper: OkHttpWrapper,
    private val root: HttpUrl,
) : EquitableClient {
    private val logger by logger()

    override suspend fun createSession(
        username: String,
        password: String,
        securityQuestions: Map<String, String>
    ): EquitableClient.EquitableClientSession? {
        val getLogOnResponseData = getLogOnPage()
        if (getLogOnResponseData == null) {
            logger.error("Failed to fetch login page!")
            return null
        }

        val cookies = mutableListOf<String>()
        cookies.addAll(getLogOnResponseData.cookies)

        val postLogInResponseCookies = postToLogIn(getLogOnResponseData.document, username, password, cookies)
        if (postLogInResponseCookies.isEmpty()) {
            logger.error("Failed to login!")
            return null
        }
        cookies.addAll(postLogInResponseCookies)

        val getSecurityQuestionResponseData = getLogOnAskSecurityQuestionPage(cookies)
        if (getSecurityQuestionResponseData == null) {
            logger.error("Failed to fetch security question page!")
            return null
        }
        cookies.addAll(getSecurityQuestionResponseData.cookies)

        val postAnswerSecurityQuestionCookies = postToLogInAnswerSecurityQuestion(
            getSecurityQuestionResponseData.document, securityQuestions, cookies
        )
        if (!postAnswerSecurityQuestionCookies.any { it.startsWith(ASPXAUTH) }) {
            logger.error("Failed to send security question answer and complete login!")
            return null
        }
        cookies.clear()
        cookies.addAll(postAnswerSecurityQuestionCookies)

        return EquitableClientSessionImpl(clientWrapper, root, cookies)
    }

    private suspend fun getLogOnPage(): OkHttpWrapper.ResponseData? {
        val getLogInUrl = root.newBuilder()
            .addPathSegments(LOG_ON_URL)
            .build()
        val getLogInRequest = Request.Builder()
            .get()
            .url(getLogInUrl)
            .build()

        return clientWrapper.fetchAndProcess(getLogInRequest)
    }

    private suspend fun postToLogIn(
        getLogInDoc: Document,
        username: String,
        password: String,
        cookies: Cookies
    ): Cookies {
        val logInForm = getLogInDoc.getElementById("sign_in") as? FormElement
        if (logInForm == null) {
            logger.warn("Unable to find login form!")
            return emptyList()
        }

        val usernameElement = logInForm.getElementById("UserName")
        if (usernameElement == null) {
            logger.warn("Unable to find username form element!")
            return emptyList()
        }

        val passwordElement = logInForm.getElementById("Password")
        if (passwordElement == null) {
            logger.warn("Unable to find password form element!")
            return emptyList()
        }

        // populate username and password
        usernameElement.value = username
        passwordElement.value = password

        val postLogInUrl = logInForm.absUrl("action").toHttpUrl()
        val body = logInForm.formData().toRequestBody()
        val postLogInRequest = Request.Builder()
            .url(postLogInUrl)
            .post(body)
            .cookies(cookies)
            .build()

        return clientWrapper.fetchRedirectCookies(postLogInRequest)
    }

    private suspend fun getLogOnAskSecurityQuestionPage(cookies: Cookies): OkHttpWrapper.ResponseData? {
        val getLogInAskSecurityQuestionUrl = root.newBuilder()
            .addPathSegments(LOG_ON_ASK_SECURITY_URL)
            .build()
        val getLogInAskSecurityQuestionRequest = Request.Builder()
            .url(getLogInAskSecurityQuestionUrl)
            .get()
            .cookies(cookies)
            .build()

        return clientWrapper.fetchAndProcess(getLogInAskSecurityQuestionRequest)
    }

    private suspend fun postToLogInAnswerSecurityQuestion(
        askSecurityDoc: Document,
        securityQuestions: Map<String, String>,
        cookies: Cookies,
    ): Cookies {
        val askSecurityForm = askSecurityDoc.getElementById("sign_in_question") as? FormElement
        if (askSecurityForm == null) {
            logger.warn("Unable to find security question form!")
            return emptyList()
        }

        val question = askSecurityForm.selectFirst("p > strong")?.text()
        if (question == null) {
            logger.warn("Unable to find security question!")
            return emptyList()
        }

        val answer = securityQuestions.getSecurityAnswer(question)
        if (answer == null) {
            logger.warn("Unable to identify security question answer!")
            return emptyList()
        }

        val answerElement = askSecurityForm.getElementById("Answer")
        if (answerElement == null) {
            logger.warn("Unable to find security answer field!")
            return emptyList()
        }

        // populate security answer
        answerElement.value = answer

        // submit
        val postSecurityAnswerUrl = askSecurityForm.absUrl("action").toHttpUrl()
        val body = askSecurityForm.formData().toRequestBody()
        val postSecurityAnswerRequest = Request.Builder()
            .url(postSecurityAnswerUrl)
            .post(body)
            .cookies(cookies)
            .build()

        return clientWrapper.fetchRedirectCookies(postSecurityAnswerRequest)
    }

    private fun Map<String, String>.getSecurityAnswer(question: String): String? = with(Locale.getDefault()) {
        mapKeys { (key, _) -> key.lowercase(this) }[question.lowercase(this)]
    }
}