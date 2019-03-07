package com.pissiphany.bany.plugin.sample

import com.pissiphany.bany.plugin.BanyPlugin
import com.pissiphany.bany.plugin.BanyPluginTransaction
import org.pf4j.Extension
import org.pf4j.Plugin
import org.pf4j.PluginWrapper
import java.time.LocalDate

class SamplePlugin(wrapper: PluginWrapper) : Plugin(wrapper) {
    @Extension
    class Dummy(override val name: String) : BanyPlugin {
        override fun setup(configuration: BanyPlugin.Configuration): Boolean {
            println("dummy plugin setup")
            return true
        }

        override fun getNewTransactionsSince(date: LocalDate?): List<BanyPluginTransaction> {
            println("dummy plugin getNewTransactionsSince()")
            return emptyList()
        }

        override fun getYnabAccountId(): String {
            println("dummy plugin teardown")
            return "ynabAccountId"
        }

        override fun tearDown() {
            println("dummy plugin teardown")
        }
    }
}