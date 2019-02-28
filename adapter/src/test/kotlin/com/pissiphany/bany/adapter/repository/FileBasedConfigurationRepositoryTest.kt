package com.pissiphany.bany.adapter.repository

import com.pissiphany.bany.adapter.config.BanyConfig
import com.pissiphany.bany.adapter.config.BanyConnection
import com.pissiphany.bany.adapter.config.BanyPlugin
import com.pissiphany.bany.adapter.mapper.BudgetAccountIdsMapper
import com.pissiphany.bany.domain.dataStructure.BudgetAccountIds
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class FileBasedConfigurationRepositoryTest {
    @Test
    fun getBudgetAccountIds() {
        val budgetAccountId1 = BudgetAccountIds("ynabBudgetId1", "ynabAccountId1")
        val budgetAccountId2 = BudgetAccountIds("ynabBudgetId2", "ynabAccountId2")
        val plugins = mapOf(
            "type1" to BanyPlugin("username1", "password1", listOf(BanyConnection("name1", "ynabBudgetId1", "ynabAccountId1", "accountId1"))),
            "type2" to BanyPlugin("username2", "password2", listOf(BanyConnection("name2", "ynabBudgetId2", "ynabAccountId2", "accountId2")))
        )
        val config = BanyConfig("token", plugins)
        val mapper = BudgetAccountIdsMapper()

        val repo = FileBasedConfigurationRepository(config, mapper)

        assertIterableEquals(listOf(budgetAccountId1, budgetAccountId2), repo.getBudgetAccountIds())
    }
}