package com.pissiphany.bany.adapter.mapper

import com.pissiphany.bany.adapter.config.Connection
import com.pissiphany.bany.domain.dataStructure.BudgetAccountIds
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class BudgetAccountIdsMapperTest {
    @Test
    fun toBudgetAccountIds() {
        val budgetId = "ynabBudgetId"
        val accountId = "ynabAccountId"
        val connection = Connection(
            name = "name", ynabBudgetId = budgetId, ynabAccountId = accountId, thirdPartyAccountId = "accountId"
        )
        val budgetAccountIds = BudgetAccountIds(budgetId = budgetId, accountId = accountId)

        assertEquals(budgetAccountIds, BudgetAccountIdsMapper().toBudgetAccountIds(connection))
    }
}