package com.pissiphany.bany.plugin

import com.pissiphany.bany.plugin.dataStructure.BanyPluginBudgetAccountIds
import com.pissiphany.bany.plugin.dataStructure.BanyPluginTransaction
import java.time.LocalDate

interface BanyPlugin {
    fun getBanyPluginBudgetAccountIds(): List<BanyPluginBudgetAccountIds>
    fun getNewBanyPluginTransactionsSince(
        budgetAccountIds: BanyPluginBudgetAccountIds, date: LocalDate?
    ): List<BanyPluginTransaction>

    interface Credentials {
        val username: String
        val password: String
        val connections: List<Connection>
        val data: Map<String, String>
    }

    interface Connection {
        val ynabBudgetId: String
        val ynabAccountId: String
        val thirdPartyAccountId: String
        val data: Map<String, String>
    }
}

interface BanyConfigurablePlugin : BanyPlugin, ConfigurablePlugin

interface ConfigurablePlugin {
    fun setup(): Boolean = true
    fun tearDown() = Unit
}
