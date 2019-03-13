package com.pissiphany.bany.adapter.repository

import com.pissiphany.bany.config.BanyConfig
import com.pissiphany.bany.adapter.mapper.BudgetAccountIdsMapper
import com.pissiphany.bany.domain.dataStructure.BudgetAccountIds
import com.pissiphany.bany.domain.repository.ConfigurationRepository

class FileBasedConfigurationRepository(
    private val config: BanyConfig, private val mapper: BudgetAccountIdsMapper
) : ConfigurationRepository {
    override fun getBudgetAccountIds(): List<BudgetAccountIds> {
        val budgetAccountIds = mutableListOf<BudgetAccountIds>()
        config.plugins
            .values
            .flatten() // flatten into one big List<BanyConfigCredentials>
            .filter { it.enabled }
            .forEach { credentials ->
                for (connection in credentials.connections) {
                    budgetAccountIds.add(mapper.toBudgetAccountIds(connection))
                }
            }

        return budgetAccountIds
    }
}