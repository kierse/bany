package com.pissiphany.bany.plugin.equitable

import com.pissiphany.bany.plugin.BanyPlugin
import com.pissiphany.bany.plugin.equitable.client.EquitableClient.*
import com.pissiphany.bany.plugin.dataStructure.BanyPluginBudgetAccountIds
import com.pissiphany.bany.plugin.dataStructure.BanyPluginAccountBalance
import com.pissiphany.bany.plugin.equitable.client.*
import com.squareup.moshi.JsonClass
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.math.BigDecimal

class EquitableLifePluginTest {
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
            ),
            Connection(
                ynabBudgetId = "budget1",
                ynabAccountId = "account4",
                thirdPartyAccountId = "thirdPartyAccount3",
                data = mutableMapOf("accountType" to "investment")
            )
        )
    )

    @Test
    fun setup() {
        val client = TestClient(TestClientSession())
        val plugin = EquitableLifePlugin(client, credentials)

        assertTrue(plugin.setup())
        assertEquals(credentials.username, client.username)
        assertEquals(credentials.password, client.password)
        assertEquals(credentials.data, client.securityQuestions)
    }

    @Test
    fun tearDown() {
        val session = TestClientSession()
        val plugin = EquitableLifePlugin(TestClient(session), credentials)

        plugin.setup()
        plugin.tearDown()
        plugin.tearDown()

        assertEquals(1, session.terminateSessionCallCount)
    }

    @Test
    fun getBanyPluginBudgetAccountIds() {
        val results = EquitableLifePlugin(TestClient(TestClientSession()), credentials)
            .getBanyPluginBudgetAccountIds()

        assertEquals(BanyPluginBudgetAccountIds("budget1", "account1"), results[0])
        assertEquals(BanyPluginBudgetAccountIds("budget1", "account2"), results[1])
        assertEquals(BanyPluginBudgetAccountIds("budget1", "account3"), results[2])
    }

    @Test
    fun `getNewBanyPluginTransactionsSince - throw on unknown connection`() {
        assertThrows<NoSuchElementException> {
            EquitableLifePlugin(TestClient(TestClientSession()), credentials).getNewBanyPluginTransactionsSince(
                BanyPluginBudgetAccountIds(ynabBudgetId = "foo", ynabAccountId = "bar"), null
            )
        }
    }

    @Test
    fun `getNewBanyPluginTransactionsSince - throw on multiple matching connection`() {
        val credentials = Credentials(
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
                    ynabAccountId = "account1",
                    thirdPartyAccountId = "thirdPartyAccount1"
                )
            )
        )

        assertThrows<IllegalArgumentException> {
            EquitableLifePlugin(TestClient(TestClientSession()), credentials).getNewBanyPluginTransactionsSince(
                BanyPluginBudgetAccountIds(ynabBudgetId = "budget1", ynabAccountId = "account1"), null
            )
        }
    }

    @ParameterizedTest(name = "getNewBanyPluginTransactionsSince - null session: {arguments}")
    @CsvSource(
        "budget1,account1", // insurance
        "budget1,account3", // investment
        "budget1,account4"  // liability
    )
    fun `getNewBanyPluginTransactionsSince - throw on null session`(budgetId: String, accountId: String) {
        assertThrows<IllegalStateException> {
            EquitableLifePlugin(TestClient(TestClientSession()), credentials).getNewBanyPluginTransactionsSince(
                BanyPluginBudgetAccountIds(ynabBudgetId = budgetId, ynabAccountId = accountId), null
            )
        }
    }

    @ParameterizedTest(name = "getNewBanyPluginTransactionsSince - invalid session: {arguments}")
    @CsvSource(
        "budget1,account1", // insurance
        "budget1,account3", // investment
        "budget1,account4"  // liability
    )
    fun `getNewBanyPluginTransactionsSince - throw on invalid session`(budgetId: String, accountId: String) {
        assertThrows<IllegalStateException> {
            with(EquitableLifePlugin(TestClient(TestClientSession()), credentials)) {
                setup()
                getNewBanyPluginTransactionsSince(
                    BanyPluginBudgetAccountIds(ynabBudgetId = budgetId, ynabAccountId = accountId), null
                )
            }
        }
    }

    @Test
    fun `getNewBanyPluginTransactionsSince - insurance`() {
        val session = TestClientSession(
            true,
            insurance = listOf(
                EquitableClientSession.InsuranceDetails(
                    loanAvailable = BigDecimal("88888.88"),
                    loanBalance = BigDecimal("9999.99")
                )
            )
        )

        with(EquitableLifePlugin(TestClient(session), credentials)) {
            setup()

            val results = getNewBanyPluginTransactionsSince(
                BanyPluginBudgetAccountIds(ynabBudgetId = "budget1", ynabAccountId = "account1"), null
            )

            assertEquals(1, results.size)
            val transaction = results.first() as BanyPluginAccountBalance
            assertEquals(BigDecimal("98888.87"), transaction.amount)
        }
    }

    @Test
    fun `getNewBanyPluginTransactionsSince - liability`() {
        val session = TestClientSession(
            true,
            insurance = listOf(
                EquitableClientSession.InsuranceDetails(
                    loanAvailable = BigDecimal("88888.88"),
                    loanBalance = BigDecimal("9999.99")
                ),
                EquitableClientSession.InsuranceDetails(
                    loanAvailable = BigDecimal("28499.75"),
                    loanBalance = BigDecimal("1500.25")
                )
            )
        )

        with(EquitableLifePlugin(TestClient(session), credentials)) {
            setup()

            val results = getNewBanyPluginTransactionsSince(
                BanyPluginBudgetAccountIds(ynabBudgetId = "budget1", ynabAccountId = "account3"), null
            )

            assertEquals(1, results.size)
            val transaction = results.first() as BanyPluginAccountBalance
            assertEquals(BigDecimal("-11500.24"), transaction.amount)
        }
    }

    @Test
    fun `getNewBanyPluginTransactionsSince - investment account`() {
        val session = TestClientSession(
            true,
            investment = EquitableClientSession.InvestmentDetails(
                totalDeposits = BigDecimal("50000.01"),
                totalWithdrawals = BigDecimal("30000.00"),
                netDeposits = BigDecimal("20000.00"),
                marketValue = BigDecimal("21500.01")
            )
        )

        with(EquitableLifePlugin(TestClient(session), credentials)) {
            setup()

            val results = getNewBanyPluginTransactionsSince(
                BanyPluginBudgetAccountIds(ynabBudgetId = "budget1", ynabAccountId = "account4"), null
            )

            assertEquals(1, results.size)
            val transaction = results.first() as BanyPluginAccountBalance
            assertEquals(BigDecimal("21500.01"), transaction.amount)
        }
    }

    internal class TestClient(private val session: EquitableClientSession) : EquitableClient {
        lateinit var username: String private set
        lateinit var password: String private set
        lateinit var securityQuestions: Map<String, String> private set

        override fun createSession(
            username: String,
            password: String,
            securityQuestions: Map<String, String>
        ): EquitableClientSession {
            this.username = username
            this.password = password
            this.securityQuestions = securityQuestions
            return session
        }
    }

    internal class TestClientSession(
        private val isValid: Boolean = false,
        private val insurance: List<EquitableClientSession.InsuranceDetails> = emptyList(),
        private val investment: EquitableClientSession.InvestmentDetails? = null
    ) : EquitableClientSession {
        private var insuranceCount = 0

        var terminateSessionCallCount: Int = 0
            private set
        override fun terminateSession() {
            terminateSessionCallCount++
        }

        override fun isValid() = isValid

        override fun checkSession() = check(isValid())

        override fun getInsuranceDetails(connection: BanyPlugin.Connection): EquitableClientSession.InsuranceDetails {
            return insurance[insuranceCount++]
        }

        override fun getInvestmentDetails(connection: BanyPlugin.Connection): EquitableClientSession.InvestmentDetails {
            return checkNotNull(investment)
        }
    }

    // Adapter used by integration test
    @JsonClass(generateAdapter = true)
    data class Credentials(
        override val username: String,
        override val password: String,
        override val connections: List<Connection>,
        override val data: Map<String, String> = emptyMap()
    ) : BanyPlugin.Credentials

    // Adapter used by integration test
    @JsonClass(generateAdapter = true)
    data class Connection(
        override val name: String,
        override val ynabBudgetId: String,
        override val ynabAccountId: String,
        override val thirdPartyAccountId: String,
        override val data: MutableMap<String, String> = mutableMapOf()
    ) : BanyPlugin.Connection
}