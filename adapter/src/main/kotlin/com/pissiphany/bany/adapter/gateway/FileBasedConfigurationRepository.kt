package com.pissiphany.bany.adapter.gateway

import com.pissiphany.bany.adapter.config.BanyConfig
import com.pissiphany.bany.domain.dataStructure.BudgetAccountIds
import com.pissiphany.bany.domain.repository.ConfigurationRepository

class FileBasedConfigurationRepository(private val config: BanyConfig) : ConfigurationRepository {
    // TODO replace with a mapper
    override fun getBudgetAccountIds(): List<BudgetAccountIds> {
        val budgetAccountIds = mutableListOf<BudgetAccountIds>()
        for (plugin in config.plugins) {
            for(connection in plugin.connections) {
                budgetAccountIds.add(BudgetAccountIds(connection.ynabBudgetId, connection.ynabAccountId))
            }
        }
    }
}