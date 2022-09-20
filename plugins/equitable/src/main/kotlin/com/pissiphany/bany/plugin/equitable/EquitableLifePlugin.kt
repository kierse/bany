package com.pissiphany.bany.plugin.equitable

import com.pissiphany.bany.plugin.BanyConfigurablePlugin
import com.pissiphany.bany.plugin.BanyPlugin
import com.pissiphany.bany.plugin.dataStructure.BanyPluginTransaction
import com.pissiphany.bany.plugin.dataStructure.BanyPluginBudgetAccountIds
import com.pissiphany.bany.plugin.dataStructure.BanyPluginAccountBalance
import com.pissiphany.bany.plugin.equitable.client.EquitableClient
import com.pissiphany.bany.shared.logger
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset

private const val PAYEE = "Equitable Life of Canada"

private fun checkSession(session: EquitableClient.EquitableClientSession?) = checkNotNull(session).apply { checkSession() }

class EquitableLifePlugin(
    override val name: String,
    private val client: EquitableClient,
    private val credentials: BanyPlugin.Credentials,
) : BanyConfigurablePlugin {
    private enum class AccountType { INVESTMENT, INSURANCE, LIABILITY }

    private var clientSession: EquitableClient.EquitableClientSession? = null
    private val logger by logger()

    override suspend fun setup(): Boolean {
        clientSession = client.createSession(
            credentials.username,
            credentials.password,
            credentials.data
        )
        return true
    }

    override suspend fun tearDown() {
        clientSession?.terminateSession()
        clientSession = null
    }

    override fun getBanyPluginBudgetAccountIds(): List<BanyPluginBudgetAccountIds> {
        return credentials.connections
            .map { BanyPluginBudgetAccountIds(
                ynabAccountId = it.ynabAccountId,
                ynabBudgetId = it.ynabBudgetId
            ) }
    }

    override suspend fun getNewBanyPluginTransactionsSince(
        budgetAccountIds: BanyPluginBudgetAccountIds,
        date: LocalDate?
    ): List<BanyPluginTransaction> {
        val connection = getConnection(budgetAccountIds)
        return when (connection.getAccountType()) {
            AccountType.INSURANCE -> getInsuranceTransaction(connection)
            AccountType.INVESTMENT -> getInvestmentTransaction(connection)
            AccountType.LIABILITY -> getInsuranceLiabilityTransaction(connection)
        }
    }

    private suspend fun getInsuranceTransaction(connection: BanyPlugin.Connection): List<BanyPluginTransaction> {
        val details = checkSession(clientSession).getInsuranceDetails(connection)
        if (details == null) {
            logger.info("No insurance details found for: ${connection.name}")
            return emptyList()
        }

        return listOf(
            BanyPluginAccountBalance(
                date = OffsetDateTime.now(ZoneOffset.UTC),
                payee = PAYEE,
                amount = details.loanAvailable + details.loanBalance
            )
        )
    }

    private suspend fun getInsuranceLiabilityTransaction(connection: BanyPlugin.Connection): List<BanyPluginTransaction> {
        val clientSession = checkSession(clientSession)

        val totalLiabilities = credentials.connections
            .filter { it != connection && it.getAccountType() == AccountType.INSURANCE }
            .mapNotNull { clientSession.getInsuranceDetails(it) }
            .fold(BigDecimal(0)) { total, account -> total - account.loanBalance }

        return listOf(
            BanyPluginAccountBalance(
                date = OffsetDateTime.now(ZoneOffset.UTC),
                payee = PAYEE,
                amount = totalLiabilities
            )
        )
    }

    private suspend fun getInvestmentTransaction(connection: BanyPlugin.Connection): List<BanyPluginTransaction> {
        val details = checkSession(clientSession).getInvestmentDetails(connection)
        if (details == null) {
            logger.info("No investment details found for: ${connection.name}")
            return emptyList()
        }

        return listOf(
            BanyPluginAccountBalance(
                date = OffsetDateTime.now(ZoneOffset.UTC),
                payee = PAYEE,
                amount = details.marketValue
            )
        )
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
}