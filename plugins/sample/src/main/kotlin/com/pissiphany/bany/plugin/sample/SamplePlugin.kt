package com.pissiphany.bany.plugin.sample

import com.pissiphany.bany.plugin.BanyConfigurablePlugin
import com.pissiphany.bany.plugin.dataStructure.BanyPluginBudgetAccountIds
import com.pissiphany.bany.plugin.dataStructure.BanyPluginTransaction
import java.time.LocalDate

class SamplePlugin : BanyConfigurablePlugin {
    override suspend fun setup(): Boolean {
        println("sample plugin setup")
        return true
    }

    override suspend fun getBanyPluginBudgetAccountIds(): List<BanyPluginBudgetAccountIds> {
        println("sample plugin getYnabBudgetAccountIds")
        return emptyList()
    }

    override suspend fun getNewBanyPluginTransactionsSince(
        budgetAccountIds: BanyPluginBudgetAccountIds, date: LocalDate?
    ): List<BanyPluginTransaction> {
        println("sample plugin getNewTransactionsSince()")
        return emptyList()
    }

    override suspend fun tearDown() {
        println("sample plugin teardown")
    }
}
