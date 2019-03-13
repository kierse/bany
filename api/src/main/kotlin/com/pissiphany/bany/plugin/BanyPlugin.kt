package com.pissiphany.bany.plugin

import com.pissiphany.bany.plugin.dataStructure.YnabBudgetAccountIds
import com.pissiphany.bany.plugin.dataStructure.BanyPluginTransaction
import org.pf4j.ExtensionPoint
import java.time.LocalDate

interface BanyPlugin : ExtensionPoint {
    val name: String

    fun setup(configuration: Configuration): Boolean
    fun tearDown()

    fun getYnabBudgetIdAccountIds(): List<YnabBudgetAccountIds>
    fun getNewBanyPluginTransactionsSince(
        ynabBudgetAccountIds: YnabBudgetAccountIds, date: LocalDate?
    ): List<BanyPluginTransaction>

    interface Configuration {
        val username: String
        val password: String
        val connections: List<Connection>
    }

    interface Connection {
        val ynabBudgetId: String
        val ynabAccountId: String
        val thirdPartyAccountId: String
    }
}