package com.pissiphany.bany.plugin.equitable.client

import com.pissiphany.bany.plugin.equitable.EquitableLifePluginTest
import com.pissiphany.bany.plugin.equitable.client.EquitableClient.EquitableClientSession
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.jsoup.Jsoup
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource
import java.math.BigDecimal

private const val POLICY_VALUES_01 = "03-PolicyValues.html"
private const val GIA_POLICY_VALUES = "05-PolicyInvestments.html"

@OptIn(ExperimentalCoroutinesApi::class)
class EquitableClientSessionImplTest {
    private val cookies = listOf(
        "$ASPXAUTH=baz"
    )

    private val connection = EquitableLifePluginTest.Connection(
        name = "connection1",
        ynabBudgetId = "budget1",
        ynabAccountId = "account1",
        thirdPartyAccountId = "thirdPartyAccount1"
    )

    @Test
    fun terminateSession() = runTest {
        val getLogOutCookies = { emptyList<String>() }
        val wrapper = TestClientWrapper(
            cookies = listOf(getLogOutCookies)
        )
        with(EquitableClientSessionImpl(wrapper, TEST_ROOT.toHttpUrl(), cookies)) {
            assertTrue(isValid())
            terminateSession()
            assertFalse(isValid())
        }


        // GET /client/en/Account/LogOut
        val getLogOutRequest = wrapper.requests[0]
        assertEquals("/$LOG_OUT_URL", getLogOutRequest.url.encodedPath)
        assertEquals("GET", getLogOutRequest.method)
        getLogOutRequest.headers.assertCookie(ASPXAUTH to "baz")
    }

    @Test
    fun `checkSession - throw exception when token is missing`() = runTest {
        val clientSession = EquitableClientSessionImpl(TestClientWrapper(), TEST_ROOT.toHttpUrl(), emptyList())
        assertThrows<IllegalStateException> { clientSession.checkSession() }
    }

    @Test
    fun getInsuranceDetails() = runTest {
        val getPolicyValuesData = { // GET /policy/en/Policy/Values/<AccountNo>
            OkHttpWrapper.ResponseData(
                document = Jsoup.parse(getHtml(POLICY_VALUES_01), TEST_ROOT),
                cookies = emptyList()
            )
        }
        val wrapper = TestClientWrapper(
            data = listOf(getPolicyValuesData)
        )

        val results = EquitableClientSessionImpl(wrapper, TEST_ROOT.toHttpUrl(), cookies)
            .getInsuranceDetails(connection)

        // GET 200 /policy/en/Policy/Values/<AccountNo>
        val getPolicyValuesRequest = wrapper.requests[0]
        assertEquals("/$POLICY_VALUES_URL/${connection.thirdPartyAccountId}", getPolicyValuesRequest.url.encodedPath)
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
    fun `getInsuranceDetails - wrapper returned null`() = runTest {
        val getPolicyValuesData = { /* GET /policy/en/Policy/Values/<AccountNo> */ null }
        val wrapper = TestClientWrapper(
            data = listOf(getPolicyValuesData)
        )

        val results = EquitableClientSessionImpl(wrapper, TEST_ROOT.toHttpUrl(), cookies)
            .getInsuranceDetails(connection)

        assertNull(results)
    }

    @ParameterizedTest(name = "getInsuranceDetails [${ParameterizedTest.INDEX_PLACEHOLDER}]")
    @ValueSource(strings = [
        "",
        """<html><body><div class="details_row"><div class="grid_4 detail_label"><p>loan balance</p></div></div></body></html>""",
    ])
    fun `getInsuranceDetails - unable to find current loan balance`(html: String) = runTest {
        val getPolicyValuesData = { // GET /policy/en/Policy/Values/<AccountNo>
            OkHttpWrapper.ResponseData(
                document = Jsoup.parse(html.trim(), TEST_ROOT),
                cookies = emptyList()
            )
        }
        val wrapper = TestClientWrapper(
            data = listOf(getPolicyValuesData)
        )

        val results = EquitableClientSessionImpl(wrapper, TEST_ROOT.toHttpUrl(), cookies)
            .getInsuranceDetails(connection)

        assertNull(results)
    }

    @ParameterizedTest(name = "getInsuranceDetails [${ParameterizedTest.INDEX_PLACEHOLDER}]")
    @ValueSource(strings = [
        """<html><body><div class="details_row"><div class="grid_4 detail_label"><p>loan balance</p></div><div class="grid_8 omega"><p>$9,999.99</p></div></div></body></html>""",
        """<html><body><div class="details_row"><div class="grid_4 detail_label"><p>loan balance</p></div><div class="grid_8 omega"><p>$9,999.99</p></div></div><div class="details_row"><div class="grid_4 detail_label"><p>loan available</p></div></div></body></html>""",
    ])
    fun `getInsuranceDetails - unable to find available loan amount`(html: String) = runTest {
        val getPolicyValuesData = { // GET /policy/en/Policy/Values/<AccountNo>
            OkHttpWrapper.ResponseData(
                document = Jsoup.parse(html, TEST_ROOT),
                cookies = emptyList()
            )
        }
        val wrapper = TestClientWrapper(
            data = listOf(getPolicyValuesData)
        )

        val results = EquitableClientSessionImpl(wrapper, TEST_ROOT.toHttpUrl(), cookies)
            .getInsuranceDetails(connection)

        assertNull(results)
    }

    @Test
    fun getInvestmentDetails() = runTest {
        val getPolicyInvestmentsData = { // GET /policy/en/Policy/Investments/<AccountNo>
            OkHttpWrapper.ResponseData(
                document = Jsoup.parse(getHtml(GIA_POLICY_VALUES), TEST_ROOT),
                cookies = emptyList()
            )
        }
        val wrapper = TestClientWrapper(
            data = listOf(getPolicyInvestmentsData)
        )

        val results = EquitableClientSessionImpl(wrapper, TEST_ROOT.toHttpUrl(), cookies)
            .getInvestmentDetails(connection)

        // GET 200 /policy/en/Policy/Investments/thirdPartyAccount3
        val getPolicyInvestments = wrapper.requests[0]
        assertEquals("/$POLICY_INVESTMENTS_URL/${connection.thirdPartyAccountId}", getPolicyInvestments.url.encodedPath)
        assertEquals("GET", getPolicyInvestments.method)
        getPolicyInvestments.headers.assertCookie(ASPXAUTH to "baz")

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

    @Test
    fun `getInvestmentDetails - wrapper returned null`() = runTest {
        val getPolicyInvestmentsData = { /* GET /policy/en/Policy/Investments/<AccountNo> */ null }
        val wrapper = TestClientWrapper(
            data = listOf(getPolicyInvestmentsData)
        )

        val results = EquitableClientSessionImpl(wrapper, TEST_ROOT.toHttpUrl(), cookies)
            .getInvestmentDetails(connection)

        assertNull(results)
    }

    internal enum class InvestmentField { ALL, TOTAL_DEPOSITS, WITHDRAWALS, NET_DEPOSITS, MARKET_VALUE }
    @ParameterizedTest(name = "getInsuranceDetails [${ParameterizedTest.INDEX_PLACEHOLDER}] ${ParameterizedTest.ARGUMENTS_WITH_NAMES_PLACEHOLDER}")
    @EnumSource(InvestmentField::class)
    internal fun `getInvestmentDetails - unable to find required data`(remove: InvestmentField) = runTest {
        val html = """
            <html>
                <body>
                    <table class="tbl_total_investments">
                        <thead>
                            <tr>
                                <th>Total Deposits</th>
                                <th>Total Withdrawals</th>
                                <th>Net Deposits</th>
                                <th>Total Market Value</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td>$50,000.01</td>
                                <td>$30,000.00</td>
                                <td>$20,000.00</td>
                                <td>$21,500.01</td>
                            </tr>
                        </tbody>
                    </table>
                </body>
            </html>
        """.trim()
        val getPolicyInvestmentsData = { // GET /policy/en/Policy/Investments/<AccountNo>
            val document = Jsoup.parse(html, TEST_ROOT)

            val table = document.selectFirst(".tbl_total_investments")
            val headings = table.select("thead > tr > th")
            val values = table.select("tbody > tr > td")

            when (remove) {
                InvestmentField.ALL -> table.remove()
                InvestmentField.TOTAL_DEPOSITS -> {
                    headings[0].remove()
                    values[0].remove()
                }
                InvestmentField.WITHDRAWALS -> {
                    headings[1].remove()
                    values[1].remove()
                }
                InvestmentField.NET_DEPOSITS -> {
                    headings[2].remove()
                    values[2].remove()
                }
                InvestmentField.MARKET_VALUE -> {
                    headings[3].remove()
                    values[3].remove()
                }
            }

            OkHttpWrapper.ResponseData(
                document = document,
                cookies = emptyList()
            )
        }
        val wrapper = TestClientWrapper(
            data = listOf(getPolicyInvestmentsData)
        )

        val results = EquitableClientSessionImpl(wrapper, TEST_ROOT.toHttpUrl(), cookies)
            .getInvestmentDetails(connection)

        assertNull(results)
    }
}
