package com.pissiphany.bany.plugin

import org.pf4j.ExtensionPoint
import java.time.LocalDate

interface BanyPlugin : ExtensionPoint {
    val name: String

    fun setup(configuration: Configuration): Boolean
    fun tearDown()

    fun getYnabAccountId(): String
    fun getNewTransactionsSince(date: LocalDate?): List<BanyPluginTransaction>

    interface Configuration {
        val username: String
        val password: String
        val connections: List<Connection>
        val enabled: Boolean
    }

    interface Connection {
        val ynabBudgetId: String
        val ynabAccountId: String
        val thirdPartyAccountId: String
    }
}