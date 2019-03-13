package com.pissiphany.bany.plugin.sample

import com.pissiphany.bany.plugin.BanyPlugin
import com.pissiphany.bany.plugin.dataStructure.BanyPluginTransaction
import com.pissiphany.bany.plugin.dataStructure.YnabBudgetAccountIds
import java.time.LocalDate

class SamplePlugin : BanyPlugin {
    override fun setup(): Boolean {
        println("sample plugin setup")
        return true
    }

    override fun getYnabBudgetAccountIds(): List<YnabBudgetAccountIds> {
        println("sample plugin getYnabBudgetAccountIds")
        return emptyList()
    }

    override fun getNewBanyPluginTransactionsSince(
        ynabBudgetAccountIds: YnabBudgetAccountIds, date: LocalDate?
    ): List<BanyPluginTransaction> {
        println("sample plugin getNewTransactionsSince()")
        return emptyList()
    }

    override fun tearDown() {
        println("sample plugin teardown")
    }
}
