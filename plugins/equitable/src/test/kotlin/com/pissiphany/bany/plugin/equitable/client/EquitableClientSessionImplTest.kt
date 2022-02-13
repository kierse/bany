package com.pissiphany.bany.plugin.equitable.client

import com.pissiphany.bany.plugin.equitable.EquitableLifePluginTest
import com.pissiphany.bany.plugin.equitable.client.EquitableClient.EquitableClientSession
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

private const val POLICY_VALUES_01 = "03-PolicyValues.html"
private const val GIA_POLICY_VALUES = "05-PolicyInvestments.html"
private const val TIMEOUT = 0L

private const val INDEX_URL = "/client/en"

@OptIn(ExperimentalCoroutinesApi::class)
class EquitableClientSessionImplTest {
    private lateinit var server: MockWebServer
    private val cookies = mapOf(ASPXAUTH to "baz")

    private val connection = EquitableLifePluginTest.Connection(
        name = "connection1",
        ynabBudgetId = "budget1",
        ynabAccountId = "account1",
        thirdPartyAccountId = "thirdPartyAccount1"
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
    fun terminateSession() = runTest {
        // GET 302 /client/en/Account/LogOut
        server.enqueue(
            MockResponse()
                .setResponseCode(302)
                .addHeader("Location", INDEX_URL)
        )

        server.start()

        val clientSession = EquitableClientSessionImpl(server.url("/").toUrl(), cookies)

        assertTrue(clientSession.isValid())
        clientSession.terminateSession()
        assertFalse(clientSession.isValid())

        // GET /client/en/Account/LogOut
        val getLogOutRequest = server.takeRequest(TIMEOUT, TimeUnit.SECONDS) ?: fail()
        assertEquals(LOG_OUT_URL, getLogOutRequest.path)
        assertEquals("GET", getLogOutRequest.method)
        getLogOutRequest.headers.assertCookie(ASPXAUTH to "baz")
    }

    @Test
    fun `checkSession - throw exception when token is missing`() = runTest {
        val clientSession = EquitableClientSessionImpl(server.url("/").toUrl(), emptyMap())
        assertThrows<IllegalStateException> { clientSession.checkSession() }
    }

    @Test
    fun getInsuranceDetails() = runTest {
        // GET 200 /policy/en/Policy/Values/<AccountNo>
        server.enqueue(MockResponse().setBody(getHtml(POLICY_VALUES_01)))

        server.start()

        val results = EquitableClientSessionImpl(server.url("/").toUrl(), cookies)
            .getInsuranceDetails(connection)

        // GET 200 /policy/en/Policy/Values/<AccountNo>
        val getPolicyValuesRequest = server.takeRequest(TIMEOUT, TimeUnit.SECONDS) ?: fail()
        val regex = """^$POLICY_VALUES_URL/${connection.thirdPartyAccountId}\?_=\d+$""".toRegex()
        assertTrue(getPolicyValuesRequest.path?.matches(regex) ?: false)
        assertEquals("GET", getPolicyValuesRequest.method)
        getPolicyValuesRequest.headers.assertCookie(ASPXAUTH to "baz")

        assertEquals(
            EquitableClientSession.InsuranceDetails(
                loanAvailable = BigDecimal("88888.88"),
                loanBalance = BigDecimal("9999.99")
            ),
            results
        )
    }

    @Test
    fun getInvestmentDetails() = runTest {
        // GET 200 /policy/en/Policy/Investments/<AccountNo>
        server.enqueue(MockResponse().setBody(getHtml(GIA_POLICY_VALUES)))

        server.start()

        val results = EquitableClientSessionImpl(server.url("/").toUrl(), cookies)
            .getInvestmentDetails(connection)

        val regex = """^$POLICY_INVESTMENTS_URL/thirdPartyAccount\d\?_=\d+$""".toRegex()

        // GET 200 /policy/en/Policy/Investments/thirdPartyAccount3
        val getPolicyInvestments = server.takeRequest(TIMEOUT, TimeUnit.SECONDS) ?: fail()
        assertTrue(getPolicyInvestments.path?.matches(regex) ?: false)
        assertEquals("GET", getPolicyInvestments.method)

        assertEquals(
            EquitableClientSession.InvestmentDetails(
                totalDeposits = BigDecimal("50000.01"),
                totalWithdrawals = BigDecimal("30000.00"),
                netDeposits = BigDecimal("20000.00"),
                marketValue = BigDecimal("21500.01")
            ),
            results
        )
    }
}
