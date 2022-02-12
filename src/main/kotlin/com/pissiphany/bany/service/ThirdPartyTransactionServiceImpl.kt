package com.pissiphany.bany.service

import com.pissiphany.bany.adapter.dataStructure.YnabTransaction
import com.pissiphany.bany.adapter.service.ThirdPartyTransactionService
import com.pissiphany.bany.mapper.BanyPluginDataMapper
import com.pissiphany.bany.plugin.BanyPlugin
import com.pissiphany.bany.adapter.dataStructure.YnabBudgetAccountIds
import java.time.LocalDate

class ThirdPartyTransactionServiceImpl(
    private val plugin: BanyPlugin, private val mapper: BanyPluginDataMapper
) : ThirdPartyTransactionService {
    override fun getYnabBudgetAccountIds(): List<YnabBudgetAccountIds> {
        return plugin.getBanyPluginBudgetAccountIds()
            .map { mapper.toYnabBudgetAccountIds(it) }
    }

    override suspend fun getNewYnabTransactionsSince(
        budgetAccountIds: YnabBudgetAccountIds, date: LocalDate?
    ): List<YnabTransaction> {
        val banyPluginBudgetAccountIds = mapper.toBanyPluginBudgetAccountIds(budgetAccountIds)
        return plugin.getNewBanyPluginTransactionsSince(banyPluginBudgetAccountIds, date)
            .map { mapper.toYnabTransaction(it, banyPluginBudgetAccountIds.ynabAccountId) }
    }
}