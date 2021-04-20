package com.pissiphany.bany.plugin.equitable.client

import com.pissiphany.bany.plugin.equitable.EquitableLifePluginTest
import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.io.File
import java.util.concurrent.TimeUnit

private val RESOURCES_FILE = File("src/test/resources/html")
private const val LOG_ON = "01-LogOn.html"
private const val LOG_ON_SECURITY = "02-LogOnAskSecurityQuestion.html"
private const val TIMEOUT = 0L

private const val LOG_IN_URL = "/client/en/Account/LogIn"
private const val LOG_IN_ANSWER_SECURITY_URL = "/client/en/Account/LogInAnswerSecurityQuestion"
private const val INDEX_URL = "/client/en"

class EquitableClientImplTest {
    private lateinit var server: MockWebServer

    private val credentials = EquitableLifePluginTest.Credentials(
        username = "username",
        password = "password",
        connections = emptyList(),
        data = mapOf("question" to "answer")
    )

    @BeforeEach
    fun before() {
        server = MockWebServer()
    }

    @AfterEach
    fun after() {
        server.shutdown()
    }

    @Test
    fun createSession() {
        // GET 200 /client/en/Account/LogOn
        server.enqueue(
            MockResponse()
                .addHeader("Set-Cookie", "__RequestVerificationToken_FOO__=foo")
                .setBody(getHtml(LOG_ON))
        )

        // POST 302 /client/en/Account/LogIn
        server.enqueue(
            MockResponse()
                .setResponseCode(302)
                .addHeader("Set-Cookie", "ASP.NET_SessionId=bar")
                .addHeader("Location", LOG_ON_ASK_SECURITY_URL)
        )

        // GET 200 /client/en/Account/LogOnAskSecurityQuestion
        server.enqueue(MockResponse().setBody(getHtml(LOG_ON_SECURITY)))

        // POST 302 /client/en/Account/LogInAnswerSecurityQuestion
        server.enqueue(
            MockResponse()
                .setResponseCode(302)
                .addHeader("Set-Cookie", "cookie1=value")
                .addHeader("Set-Cookie", "cookie2=value")
                .addHeader("Set-Cookie", "$ASPXAUTH=baz")
                .addHeader("Location", INDEX_URL)
        )

        server.start()

        val client = EquitableClientImpl(server.url("/").toUrl())

        val clientSession = client.createSession(
            username = credentials.username,
            password = credentials.password,
            securityQuestions = credentials.data
        )

        // GET /client/en/Account/LogOn
        val getLogOnRequest = server.takeRequest(TIMEOUT, TimeUnit.SECONDS) ?: fail()
        assertEquals(LOG_ON_URL, getLogOnRequest.path)
        assertEquals("GET", getLogOnRequest.method)

        // POST /client/en/Account/LogIn
        val postLogInRequest = server.takeRequest(TIMEOUT, TimeUnit.SECONDS) ?: fail()
        assertEquals(LOG_IN_URL, postLogInRequest.path)
        assertEquals("POST", postLogInRequest.method)
        postLogInRequest.headers.assertCookie("__RequestVerificationToken_FOO__" to "foo")

        val postLogInRequestUrl = postLogInRequest.urlWithQueryParams()
        assertEquals(credentials.username, postLogInRequestUrl.queryParameter("UserName"))
        assertEquals(credentials.password, postLogInRequestUrl.queryParameter("Password"))
        assertEquals("token", postLogInRequestUrl.queryParameter("__RequestVerificationToken"))

        // GET /client/en/Account/LogOnAskSecurityQuestion
        val getSecurityRequest = server.takeRequest(TIMEOUT, TimeUnit.SECONDS) ?: fail()
        assertEquals(LOG_ON_ASK_SECURITY_URL, getSecurityRequest.path)
        assertEquals("GET", getSecurityRequest.method)
        getSecurityRequest.headers.assertCookie(
            "__RequestVerificationToken_FOO__" to "foo",
            "ASP.NET_SessionId" to "bar"
        )

        // POST /client/en/Account/LogInAnswerSecurityQuestion
        val postSecurityRequest = server.takeRequest(TIMEOUT, TimeUnit.SECONDS) ?: fail()
        assertEquals(LOG_IN_ANSWER_SECURITY_URL, postSecurityRequest.path)
        assertEquals("POST", postSecurityRequest.method)

        val postSecurityRequestUrl = postSecurityRequest.requestUrl
            ?.newBuilder()
            ?.encodedQuery(postSecurityRequest.body.readUtf8())
            ?.build()
            ?: fail()
        assertEquals(
            credentials.data["question"]?.toLowerCase(),
            postSecurityRequestUrl.queryParameter("Answer")?.toLowerCase()
        )
        assertEquals("token", postLogInRequestUrl.queryParameter("__RequestVerificationToken"))
        getSecurityRequest.headers.assertCookie(
            "__RequestVerificationToken_FOO__" to "foo",
            "ASP.NET_SessionId" to "bar"
        )

        assertTrue(clientSession.isValid())
    }

    @Test
    fun `createSession - missing session token`() {
        // GET 200 /client/en/Account/LogOn
        server.enqueue(
            MockResponse()
                .addHeader("Set-Cookie", "__RequestVerificationToken_FOO__=foo")
                .setBody(getHtml(LOG_ON))
        )

        // POST 302 /client/en/Account/LogIn
        server.enqueue(
            MockResponse()
                .setResponseCode(302)
                .addHeader("Set-Cookie", "ASP.NET_SessionId=bar")
                .addHeader("Location", LOG_ON_ASK_SECURITY_URL)
        )

        // GET 200 /client/en/Account/LogOnAskSecurityQuestion
        server.enqueue(MockResponse().setBody(getHtml(LOG_ON_SECURITY)))

        // POST 302 /client/en/Account/LogInAnswerSecurityQuestion
        server.enqueue(
            MockResponse()
                .setResponseCode(302)
                .addHeader("Set-Cookie", "cookie1=value")
                .addHeader("Set-Cookie", "cookie2=value")
                .addHeader("Location", INDEX_URL)
        )

        server.start()

        val client = EquitableClientImpl(server.url("/").toUrl())

        assertThrows<IllegalStateException> {
            client.createSession(
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