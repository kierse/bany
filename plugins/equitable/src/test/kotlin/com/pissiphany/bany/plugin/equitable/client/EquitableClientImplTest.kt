package com.pissiphany.bany.plugin.equitable.client

import com.pissiphany.bany.plugin.equitable.EquitableLifePluginTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.mockwebserver.RecordedRequest
import org.jsoup.Jsoup
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.io.File
import java.util.*
import kotlin.NoSuchElementException

internal const val TEST_ROOT = "http://172.0.0.1:8080"

private val RESOURCES_FILE = File("src/test/resources/html")
private const val LOG_ON = "01-LogOn.html"
private const val LOG_ON_SECURITY = "02-LogOnAskSecurityQuestion.html"

private const val LOG_IN_URL = "/client/en/Account/LogIn"
private const val LOG_IN_ANSWER_SECURITY_URL = "/client/en/Account/LogInAnswerSecurityQuestion"

@OptIn(ExperimentalCoroutinesApi::class)
class EquitableClientImplTest {
    private val credentials = EquitableLifePluginTest.Credentials(
        username = "username",
        password = "password",
        connections = emptyList(),
        data = mapOf("question" to "answer")
    )

    @Test
    fun createSession() = runTest {
        val getLogOn = {
            OkHttpWrapper.ResponseData(
                document = Jsoup.parse(getHtml(LOG_ON), TEST_ROOT),
                cookies = listOf("__RequestVerificationToken_FOO__=foo")
            )
        }
        val postLogIn = {
            listOf(
                "ASP.NET_SessionId=bar",
            )
        }
        val getLogOnSecurityQuestion = {
            OkHttpWrapper.ResponseData(
                document = Jsoup.parse(getHtml(LOG_ON_SECURITY), TEST_ROOT),
                cookies = emptyList()
            )
        }
        val postLogInAnswerSecurityQuestion = {
            listOf(
                "cookie1=value",
                "cookie2=value",
                "$ASPXAUTH=baz",
            )
        }

        val wrapper = TestClientWrapper(
            data = listOf(getLogOn, getLogOnSecurityQuestion),
            cookies = listOf(postLogIn, postLogInAnswerSecurityQuestion)
        )
        val clientSession = createClientSession(wrapper)

        // GET /client/en/Account/LogOn
        val getLogOnRequest = wrapper.requests[0]
        assertEquals("/$LOG_ON_URL", getLogOnRequest.url.encodedPath)
        assertEquals("GET", getLogOnRequest.method)

        // POST /client/en/Account/LogIn
        val postLogInRequest = wrapper.requests[1]
        assertEquals(LOG_IN_URL, postLogInRequest.url.encodedPath)
        assertEquals("POST", postLogInRequest.method)
        postLogInRequest.headers.assertCookie("__RequestVerificationToken_FOO__" to "foo")

        val expectedLogInData = mapOf(
            "UserName" to credentials.username,
            "Password" to credentials.password,
            "__RequestVerificationToken" to "token"
        )
        val postLogInRequestFormBody = postLogInRequest.body as FormBody
        assertTrue(postLogInRequestFormBody.size <= expectedLogInData.size)
        (0 until postLogInRequestFormBody.size).forEach { i ->
            val key = postLogInRequestFormBody.name(i)
            assertEquals(expectedLogInData[key], postLogInRequestFormBody.value(i))
        }

        // GET /client/en/Account/LogOnAskSecurityQuestion
        val getSecurityRequest = wrapper.requests[2]
        assertEquals("/$LOG_ON_ASK_SECURITY_URL", getSecurityRequest.url.encodedPath)
        assertEquals("GET", getSecurityRequest.method)
        getSecurityRequest.headers.assertCookie(
            "__RequestVerificationToken_FOO__" to "foo",
            "ASP.NET_SessionId" to "bar"
        )

        // POST /client/en/Account/LogInAnswerSecurityQuestion
        val postSecurityRequest = wrapper.requests[3]
        assertEquals(LOG_IN_ANSWER_SECURITY_URL, postSecurityRequest.url.encodedPath)
        assertEquals("POST", postSecurityRequest.method)

        val postSecurityRequestFormBody = postSecurityRequest.body as FormBody
        val locale = Locale.getDefault()

        val expectedPostSecurityData = mapOf(
            "Answer" to credentials.data["question"]?.lowercase(locale),
            "__RequestVerificationToken" to "token"
        )

        assertEquals(expectedPostSecurityData.size, postSecurityRequestFormBody.size)
        (0 until postSecurityRequestFormBody.size).forEach { i ->
            val key = postSecurityRequestFormBody.name(i)
            assertEquals(expectedPostSecurityData[key], postSecurityRequestFormBody.value(i).lowercase(locale))
        }
        getSecurityRequest.headers.assertCookie(
            "__RequestVerificationToken_FOO__" to "foo",
            "ASP.NET_SessionId" to "bar"
        )

        assertEquals(true, clientSession?.isValid())
    }

    @Test
    fun `createSession - wrapper returned null for getLogOnPage`() = runTest {
        val data = { /* GET /client/en/Account/LogOn */ null }
        val wrapper = TestClientWrapper(
            data = listOf(data)
        )

        assertNull(createClientSession(wrapper))
    }

    @Test
    fun `createSession - wrapper returned null for postToLogIn`() = runTest {
        val data = { // GET /client/en/Account/LogOn
            OkHttpWrapper.ResponseData(
                document = Jsoup.parse(getHtml(LOG_ON), TEST_ROOT),
                cookies = listOf("__RequestVerificationToken_FOO__=foo")
            )
        }
        val cookies = { /* POST /client/en/Account/LogIn */ emptyList<String>() }
        val wrapper = TestClientWrapper(
            data = listOf(data),
            cookies = listOf(cookies)
        )

        assertNull(createClientSession(wrapper))
    }

    @Test
    fun `createSession - wrapper returned null for getLogOnAskSecurityQuestionPage`() = runTest {
        val getLogOn = { // GET /client/en/Account/LogOn
            OkHttpWrapper.ResponseData(
                document = Jsoup.parse(getHtml(LOG_ON), TEST_ROOT),
                cookies = listOf("__RequestVerificationToken_FOO__=foo")
            )
        }
        val getLogOnSecurityQuestion = { // GET /client/en/Account/LogOnAskSecurityQuestion
            OkHttpWrapper.ResponseData(
                document = Jsoup.parse(getHtml(LOG_ON_SECURITY), TEST_ROOT),
                cookies = emptyList()
            )
        }
        val postToLogIn = { /* POST /client/en/Account/LogIn */ emptyList<String>() }
        val wrapper = TestClientWrapper(
            data = listOf(getLogOn, getLogOnSecurityQuestion),
            cookies = listOf(postToLogIn)
        )

        assertNull(createClientSession(wrapper))
    }

    @Test
    fun `createSession - wrapper returned null for postToLogInAnswerSecurityQuestion`() = runTest {
        val getLogOn = { // GET /client/en/Account/LogOn
            OkHttpWrapper.ResponseData(
                document = Jsoup.parse(getHtml(LOG_ON), TEST_ROOT),
                cookies = listOf("__RequestVerificationToken_FOO__=foo")
            )
        }
        val getLogOnSecurityQuestion = { // GET /client/en/Account/LogOnAskSecurityQuestion
            OkHttpWrapper.ResponseData(
                document = Jsoup.parse(getHtml(LOG_ON_SECURITY), TEST_ROOT),
                cookies = emptyList()
            )
        }
        val postToLogIn = { // POST /client/en/Account/LogIn
            listOf(
                "__RequestVerificationToken_FOO__=foo",
                "ASP.NET_SessionId=bar"
            )
        }
        val postToLogInAnswerSecurityQuestion = { /* POST /client/en/Account/LogInAnswerSecurityQuestion */ emptyList<String>() }
        val wrapper = TestClientWrapper(
            data = listOf(getLogOn, getLogOnSecurityQuestion),
            cookies = listOf(postToLogIn, postToLogInAnswerSecurityQuestion)
        )

        assertNull(createClientSession(wrapper))
    }

    private suspend fun createClientSession(
        clientWrapper: OkHttpWrapper
    ): EquitableClient.EquitableClientSession? {
        return with(EquitableClientImpl(clientWrapper, TEST_ROOT.toHttpUrl())) {
            createSession(
                username = credentials.username,
                password = credentials.password,
                securityQuestions = credentials.data
            )
        }
    }
}

internal fun Headers.assertCookie(vararg cookies: Pair<String, String>) {
    val found = get("Cookie")
        ?.split(';')
        ?.map(String::trim)
        ?.map {
            val (key, value) = it.split('=')
            key to value
        }
        ?: emptyList()
    val missing = cookies.toList().minus(found)

    if (missing.isEmpty()) return

    fail { "The following cookies were missing:\n${missing.joinToString("\n")}" }
}

internal fun RecordedRequest.urlWithQueryParams(): HttpUrl =
    this.requestUrl
        ?.newBuilder()
        ?.encodedQuery(body.readUtf8())
        ?.build()
        ?: fail()

internal fun getHtml(name: String) = File(RESOURCES_FILE, name).readText()

internal class TestClientWrapper(
    data: List<() -> OkHttpWrapper.ResponseData?> = emptyList(),
    cookies: List<() -> Cookies> = emptyList()
) : OkHttpWrapper {
    private val data: MutableList<() -> OkHttpWrapper.ResponseData?> = data.reversed().toMutableList()
    private val cookies: MutableList<() -> Cookies> = cookies.reversed().toMutableList()

    private val _requests = mutableListOf<Request>()
    val requests: List<Request>
        get() = _requests

    override suspend fun fetchAndProcess(request: Request): OkHttpWrapper.ResponseData? {
        _requests.add(request)

        if (data.isEmpty()) throw NoSuchElementException("No data lambda for: ${request.url.encodedPath}")
        return data.removeLast().invoke()
    }

    override suspend fun fetchRedirectCookies(request: Request): Cookies {
        _requests.add(request)

        if (cookies.isEmpty()) throw NoSuchElementException("No cookie lambda for: ${request.url.encodedPath}")
        return cookies.removeLast().invoke()
    }
}