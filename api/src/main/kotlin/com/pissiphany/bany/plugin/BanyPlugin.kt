package com.pissiphany.bany.plugin

import com.pissiphany.bany.plugin.dataStructure.BanyPluginBudgetAccountIds
import com.pissiphany.bany.plugin.dataStructure.BanyPluginTransaction
import java.time.LocalDate

interface BanyPlugin {
    fun getBanyPluginBudgetAccountIds(): List<BanyPluginBudgetAccountIds>
    suspend fun getNewBanyPluginTransactionsSince(
        budgetAccountIds: BanyPluginBudgetAccountIds, date: LocalDate?
    ): List<BanyPluginTransaction>

    interface Credentials {
        val username: String
        val password: String
        val connections: List<Connection>
        val data: Map<String, String>
    }

    interface Connection {
        val name: String
        val ynabBudgetId: String
        val ynabAccountId: String
        val thirdPartyAccountId: String
        val data: Map<String, String>
    }
}

interface BanyConfigurablePlugin : BanyPlugin, ConfigurablePlugin

interface ConfigurablePlugin {
    suspend fun setup(): Boolean = true
    suspend fun tearDown() = Unit
}
