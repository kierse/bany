package com.pissiphany.bany.plugin

import com.pissiphany.bany.plugin.dataStructure.YnabBudgetAccountIds
import com.pissiphany.bany.plugin.dataStructure.BanyPluginTransaction
import java.time.LocalDate

interface BanyPlugin {
    fun setup(): Boolean = true
    fun tearDown() = Unit

    // TODO should take/return BanyPlugin* here not Ynab*
    fun getYnabBudgetAccountIds(): List<YnabBudgetAccountIds>
    fun getNewBanyPluginTransactionsSince(
        ynabBudgetAccountIds: YnabBudgetAccountIds, date: LocalDate?
    ): List<BanyPluginTransaction>

    interface Credentials {
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