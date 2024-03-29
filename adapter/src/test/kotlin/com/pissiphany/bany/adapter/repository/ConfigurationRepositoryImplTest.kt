package com.pissiphany.bany.adapter.repository

import com.pissiphany.bany.adapter.dataStructure.YnabConnection
import com.pissiphany.bany.adapter.dataStructure.YnabCredentials
import com.pissiphany.bany.adapter.mapper.YnabBudgetAccountIdsMapper
import com.pissiphany.bany.domain.dataStructure.BudgetAccountIds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

@OptIn(ExperimentalCoroutinesApi::class)
internal class ConfigurationRepositoryImplTest {
    @Test
    fun getBudgetAccountIds() = runTest {
        val budgetAccountId1 = BudgetAccountIds("name1", "ynabBudgetId1", "ynabAccountId1")
        val budgetAccountId2 = BudgetAccountIds("name2", "ynabBudgetId2", "ynabAccountId2")
        val credentials = listOf(
            YnabCredentials(
                "username1",
                "password1",
                listOf(
                    YnabConnection(
                        "name1",
                        "ynabBudgetId1",
                        "ynabAccountId1",
                        "accountId1"
                    )
                ),
                emptyMap()
            ),
            YnabCredentials(
                "username2",
                "password2",
                listOf(
                    YnabConnection(
                        "name2",
                        "ynabBudgetId2",
                        "ynabAccountId2",
                        "accountId2"
                    )
                ),
                emptyMap()
            )
        )
        val mapper = YnabBudgetAccountIdsMapper()

        val repo = ConfigurationRepositoryImpl(credentials, mapper)

        assertIterableEquals(listOf(budgetAccountId1, budgetAccountId2), repo.getBudgetAccountIds())
    }
}