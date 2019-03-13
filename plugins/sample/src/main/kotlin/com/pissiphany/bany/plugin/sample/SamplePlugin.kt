package com.pissiphany.bany.plugin.sample

import com.pissiphany.bany.plugin.BanyPlugin
import com.pissiphany.bany.plugin.dataStructure.BanyPluginTransaction
import com.pissiphany.bany.plugin.dataStructure.YnabBudgetAccountIds
import org.pf4j.Extension
import java.time.LocalDate

@Extension
class SamplePlugin : BanyPlugin {
    override val name: String = "sample"

    override fun setup(credentials: BanyPlugin.Credentials): Boolean {
        println("dummy plugin setup")
        return true
    }

    override fun getYnabBudgetAccountIds(): List<YnabBudgetAccountIds> {
        println("dummy plugin getYnabBudgetAccountIds")
        return emptyList()
    }

    override fun getNewBanyPluginTransactionsSince(
        ynabBudgetAccountIds: YnabBudgetAccountIds, date: LocalDate?
    ): List<BanyPluginTransaction> {
        println("dummy plugin getNewTransactionsSince()")
        return emptyList()
    }

    override fun tearDown() {
        println("dummy plugin teardown")
    }
}
