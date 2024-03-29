package com.pissiphany.bany.adapter.repository

import com.pissiphany.bany.adapter.dataStructure.YnabCredentials
import com.pissiphany.bany.adapter.mapper.YnabBudgetAccountIdsMapper
import com.pissiphany.bany.domain.dataStructure.BudgetAccountIds
import com.pissiphany.bany.domain.repository.ConfigurationRepository

class ConfigurationRepositoryImpl(
    private val ynabCredentials: List<YnabCredentials>, private val mapper: YnabBudgetAccountIdsMapper
) : ConfigurationRepository {
    override suspend fun getBudgetAccountIds(): List<BudgetAccountIds> {
        val budgetAccountIds = mutableListOf<BudgetAccountIds>()
        ynabCredentials
            .filter { it.enabled }
            .forEach { credentials ->
                for (connection in credentials.connections) {
                    budgetAccountIds.add(mapper.toBudgetAccountIds(connection))
                }
            }

        return budgetAccountIds
    }
}