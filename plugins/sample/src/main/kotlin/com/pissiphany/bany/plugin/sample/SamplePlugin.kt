package com.pissiphany.bany.plugin.sample

import com.pissiphany.bany.plugin.BanyPlugin
import com.pissiphany.bany.plugin.dataStructure.BanyPluginTransaction
import com.pissiphany.bany.plugin.dataStructure.YnabBudgetAccountIds
import org.pf4j.Extension
import org.pf4j.Plugin
import org.pf4j.PluginWrapper
import java.time.LocalDate

class SamplePlugin(wrapper: PluginWrapper) : Plugin(wrapper) {
    @Extension
    class Sample : BanyPlugin {
        override val name: String = "sample"

        override fun setup(configuration: BanyPlugin.Configuration): Boolean {
            println("dummy plugin setup")
            return true
        }

        override fun getYnabBudgetAccountIds(): List<YnabBudgetAccountIds> {
            println("dummy plugin getYnabBudgetAccountIds")
            return emptyList()
        }

        override fun getNewBanyPluginTransactionsSince(
            ynabBudgetAccountIds: YnabBudgetAccountIds,
            date: LocalDate?
        ): List<BanyPluginTransaction> {
            println("dummy plugin getNewTransactionsSince()")
            return emptyList()
        }

        override fun tearDown() {
            println("dummy plugin teardown")
        }
    }
}