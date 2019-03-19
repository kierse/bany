package com.pissiphany.bany.service

import com.pissiphany.bany.adapter.dataStructure.YnabTransaction
import com.pissiphany.bany.adapter.mapper.BanyPluginTransactionMapper
import com.pissiphany.bany.adapter.service.ThirdPartyTransactionService
import com.pissiphany.bany.plugin.BanyPlugin
import com.pissiphany.bany.plugin.dataStructure.YnabBudgetAccountIds
import java.time.LocalDate

class ThirdPartyTransactionServiceImpl(
    internal val plugin: BanyPlugin, private val mapper: BanyPluginTransactionMapper
) : ThirdPartyTransactionService {
    override fun getYnabBudgetAccountIds(): List<YnabBudgetAccountIds> {
        return plugin.getYnabBudgetAccountIds()
    }

    override fun getNewYnabTransactionsSince(
        budgetAccountIds: YnabBudgetAccountIds, date: LocalDate?
    ): List<YnabTransaction> {
        return plugin
            .getNewBanyPluginTransactionsSince(budgetAccountIds, date)
            .map { mapper.toYnabTransaction(it, budgetAccountIds.ynabAccountId) }
    }
}