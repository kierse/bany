package com.pissiphany.bany.plugin.equitable

import com.pissiphany.bany.plugin.BanyPlugin
import com.pissiphany.bany.plugin.dataStructure.BanyPluginBudgetAccountIds
import com.pissiphany.bany.plugin.dataStructure.BanyPluginAccountBalance
import com.squareup.moshi.JsonClass
import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.io.File
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

private val RESOURCES_FILE = File("src/test/resources/html")
private const val LOG_ON = "01-LogOn.html"
private const val LOG_ON_SECURITY = "02-LogOnAskSecurityQuestion.html"
private const val POLICY_VALUES_01 = "03-PolicyValues.html"
private const val POLICY_VALUES_02 = "04-PolicyValues.html"
private const val TIMEOUT = 0L

private const val LOG_IN_URL = "/client/en/Account/LogIn"
private const val LOG_IN_ANSWER_SECURITY_URL = "/client/en/Account/LogInAnswerSecurityQuestion"
private const val INDEX_URL = "/client/en"

class EquitableLifePluginTest {
    private lateinit var server: MockWebServer

    private val credentials = Credentials(
        username = "username",
        password = "password",
        connections = listOf(
            Connection(
                ynabBudgetId = "budget1",
                ynabAccountId = "account1",
                thirdPartyAccountId = "thirdPartyAccount1"
            ),
            Connection(
                ynabBudgetId = "budget1",
                ynabAccountId = "account2",
                thirdPartyAccountId = "thirdPartyAccount2"
            ),
            Connection(
                ynabBudgetId = "budget1",
                ynabAccountId = "account3",
                thirdPartyAccountId = "",
                data = mutableMapOf("accountType" to "liability")
            )
        ),
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
    fun setup() {
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
                .addHeader("Location", EquitableLifePlugin.LOG_ON_ASK_SECURITY_URL)
        )

        // GET 200 /client/en/Account/LogOnAskSecurityQuestion
        server.enqueue(MockResponse().setBody(getHtml(LOG_ON_SECURITY)))

        // POST 302 /client/en/Account/LogInAnswerSecurityQuestion
        server.enqueue(
            MockResponse()
                .setResponseCode(302)
                .addHeader("Set-Cookie", "cookie1=value")
                .addHeader("Set-Cookie", "cookie2=value")
                .addHeader("Set-Cookie", "${EquitableLifePlugin.ASPXAUTH}=baz")
                .addHeader("Location", INDEX_URL)
        )

        server.start()

        val plugin = EquitableLifePlugin(credentials, server.url("/").toUrl())
        val backdoor = plugin.TestBackdoor()

        val result = plugin.setup()

        // GET /client/en/Account/LogOn
        val getLogOnRequest = server.takeRequest(TIMEOUT, TimeUnit.SECONDS) ?: fail()
        assertEquals(EquitableLifePlugin.LOG_ON_URL, getLogOnRequest.path)
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
        assertEquals(EquitableLifePlugin.LOG_ON_ASK_SECURITY_URL, getSecurityRequest.path)
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

        assertEquals(
            mapOf(
                "cookie1" to "value",
                "cookie2" to "value",
                EquitableLifePlugin.ASPXAUTH to "baz"
            ),
            backdoor.sessionCookies
        )
        assertTrue(result)
    }

    private fun Headers.assertCookie(vararg cookies: Pair<String, String>) {
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

    private fun RecordedRequest.urlWithQueryParams(): HttpUrl =
        this.requestUrl
            ?.newBuilder()
            ?.encodedQuery(body.readUtf8())
            ?.build()
            ?: fail()

    @Test
    fun tearDown() {
        // GET 302 /client/en/Account/LogOut
        server.enqueue(
            MockResponse()
                .setResponseCode(302)
                .addHeader("Location", INDEX_URL)
        )

        server.start()

        val plugin = EquitableLifePlugin(credentials, server.url("/").toUrl())
        val backdoor = plugin.TestBackdoor()
            .apply {
                sessionCookies = mapOf(EquitableLifePlugin.ASPXAUTH to "baz")
            }

        plugin.tearDown()

        // GET /client/en/Account/LogOut
        val getLogOutRequest = server.takeRequest(TIMEOUT, TimeUnit.SECONDS) ?: fail()
        assertEquals(EquitableLifePlugin.LOG_OUT_URL, getLogOutRequest.path)
        assertEquals("GET", getLogOutRequest.method)
        getLogOutRequest.headers.assertCookie(
            EquitableLifePlugin.ASPXAUTH to "baz"
        )

        assertTrue(backdoor.sessionCookies.isEmpty())
    }

    @Test
    fun getBanyPluginBudgetAccountIds() {
        val results = EquitableLifePlugin(credentials, server.url("/").toUrl())
            .getBanyPluginBudgetAccountIds()

        assertEquals(BanyPluginBudgetAccountIds("budget1", "account1"), results[0])
        assertEquals(BanyPluginBudgetAccountIds("budget1", "account2"), results[1])
        assertEquals(BanyPluginBudgetAccountIds("budget1", "account3"), results[2])
    }

    @Test
    fun `getNewBanyPluginTransactionsSince - insurance`() {
        // GET 200 /policy/en/Policy/Values/<AccountNo>
        server.enqueue(MockResponse().setBody(getHtml(POLICY_VALUES_01)))

        server.start()

        val plugin = EquitableLifePlugin(credentials, server.url("/").toUrl())
        plugin.TestBackdoor()
            .apply {
                sessionCookies = mapOf(EquitableLifePlugin.ASPXAUTH to "baz")
            }

        val results = plugin.getNewBanyPluginTransactionsSince(
            BanyPluginBudgetAccountIds(ynabBudgetId = "budget1", ynabAccountId = "account1"), null
        )

        assertEquals(1, results.size)
        val transaction = results.first() as BanyPluginAccountBalance
        assertEquals(BigDecimal("98888.87"), transaction.amount)

        // GET 200 /policy/en/Policy/Values/<AccountNo>
        val getPolicyValuesRequest = server.takeRequest(TIMEOUT, TimeUnit.SECONDS) ?: fail()
        val regex = """^${EquitableLifePlugin.POLICY_VALUES_URL}/thirdPartyAccount1\?_=\d+$""".toRegex()
        assertTrue(getPolicyValuesRequest.path?.matches(regex) ?: false)
        assertEquals("GET", getPolicyValuesRequest.method)
        getPolicyValuesRequest.headers.assertCookie(
            EquitableLifePlugin.ASPXAUTH to "baz"
        )
    }

    @Test
    fun `getNewBanyPluginTransactionsSince - liability`() {
        // GET 200 /policy/en/Policy/Values/<AccountNo>
        server.enqueue(MockResponse().setBody(getHtml(POLICY_VALUES_01)))
        server.enqueue(MockResponse().setBody(getHtml(POLICY_VALUES_02)))

        server.start()

        val plugin = EquitableLifePlugin(credentials, server.url("/").toUrl())
        plugin.TestBackdoor()
            .apply {
                sessionCookies = mapOf(EquitableLifePlugin.ASPXAUTH to "baz")
            }

        val results = plugin.getNewBanyPluginTransactionsSince(
            BanyPluginBudgetAccountIds(ynabBudgetId = "budget1", ynabAccountId = "account3"), null
        )

        assertEquals(1, results.size)
        val transaction = results.first() as BanyPluginAccountBalance
        assertEquals(BigDecimal("-11500.24"), transaction.amount)

        val regex = """^${EquitableLifePlugin.POLICY_VALUES_URL}/thirdPartyAccount\d\?_=\d+$""".toRegex()

        // GET 200 /policy/en/Policy/Values/thirdPartyAccount1
        val getPolicyValuesRequest1 = server.takeRequest(TIMEOUT, TimeUnit.SECONDS) ?: fail()
        assertTrue(getPolicyValuesRequest1.path?.matches(regex) ?: false)
        assertEquals("GET", getPolicyValuesRequest1.method)

        // GET 200 /policy/en/Policy/Values/thirdPartyAccount2
        val getPolicyValuesRequest2 = server.takeRequest(TIMEOUT, TimeUnit.SECONDS) ?: fail()
        assertTrue(getPolicyValuesRequest2.path?.matches(regex) ?: false)
        assertEquals("GET", getPolicyValuesRequest2.method)

        getPolicyValuesRequest2.headers.assertCookie(
            EquitableLifePlugin.ASPXAUTH to "baz"
        )
    }

    @JsonClass(generateAdapter = true)
    data class Credentials(
        override val username: String,
        override val password: String,
        override val connections: List<Connection>,
        override val data: Map<String, String> = emptyMap()
    ) : BanyPlugin.Credentials

    @JsonClass(generateAdapter = true)
    data class Connection(
        override val ynabBudgetId: String,
        override val ynabAccountId: String,
        override val thirdPartyAccountId: String,
        override val data: MutableMap<String, String> = mutableMapOf()
    ) : BanyPlugin.Connection

    private fun getHtml(name: String) = File(RESOURCES_FILE, name).readText()
}