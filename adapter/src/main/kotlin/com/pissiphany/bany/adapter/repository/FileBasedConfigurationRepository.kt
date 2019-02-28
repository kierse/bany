package com.pissiphany.bany.adapter.repository

import com.pissiphany.bany.adapter.config.BanyConfig
import com.pissiphany.bany.adapter.mapper.BudgetAccountIdsMapper
import com.pissiphany.bany.domain.dataStructure.BudgetAccountIds
import com.pissiphany.bany.domain.repository.ConfigurationRepository

class FileBasedConfigurationRepository(
    private val config: BanyConfig, private val mapper: BudgetAccountIdsMapper
) : ConfigurationRepository {
    override fun getBudgetAccountIds(): List<BudgetAccountIds> {
        val budgetAccountIds = mutableListOf<BudgetAccountIds>()
        config.plugins
            .filterValues { it.enabled }
            .forEach { (_, plugin) ->
                for(connection in plugin.connections) {
                    budgetAccountIds.add(mapper.toBudgetAccountIds(connection))
                }
            }

        return budgetAccountIds
    }
}