package com.pissiphany.bany.adapter.repository

import com.pissiphany.bany.adapter.dataStructure.YnabConnection
import com.pissiphany.bany.adapter.dataStructure.YnabCredentials
import com.pissiphany.bany.adapter.mapper.YnabBudgetAccountIdsMapper
import com.pissiphany.bany.domain.dataStructure.BudgetAccountIds
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class ConfigurationRepositoryImplTest {
    @Test
    fun getBudgetAccountIds() {
        val budgetAccountId1 = BudgetAccountIds("ynabBudgetId1", "ynabAccountId1")
        val budgetAccountId2 = BudgetAccountIds("ynabBudgetId2", "ynabAccountId2")
        val plugins = mapOf(
            "type1" to listOf(YnabCredentials(
                "username1",
                "password1",
                listOf(
                    YnabConnection(
                        "name1",
                        "ynabBudgetId1",
                        "ynabAccountId1",
                        "accountId1"
                    )
                )
            )
            ),
            "type2" to listOf(YnabCredentials(
                "username2",
                "password2",
                listOf(
                    YnabConnection(
                        "name2",
                        "ynabBudgetId2",
                        "ynabAccountId2",
                        "accountId2"
                    )
                )
            ))
        )
        val mapper = YnabBudgetAccountIdsMapper()

        val repo = ConfigurationRepositoryImpl(plugins, mapper)

        assertIterableEquals(listOf(budgetAccountId1, budgetAccountId2), repo.getBudgetAccountIds())
    }
}