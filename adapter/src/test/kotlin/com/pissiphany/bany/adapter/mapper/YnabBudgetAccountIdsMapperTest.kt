package com.pissiphany.bany.adapter.mapper

import com.pissiphany.bany.adapter.dataStructure.YnabConnection
import com.pissiphany.bany.domain.dataStructure.BudgetAccountIds
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class YnabBudgetAccountIdsMapperTest {
    @Test
    fun toBudgetAccountIds() {
        val budgetId = "ynabBudgetId"
        val accountId = "ynabAccountId"
        val connection = YnabConnection(
            name = "name", ynabBudgetId = budgetId, ynabAccountId = accountId, thirdPartyAccountId = "accountId"
        )
        val budgetAccountIds = BudgetAccountIds(budgetId = budgetId, accountId = accountId)

        assertEquals(budgetAccountIds, YnabBudgetAccountIdsMapper().toBudgetAccountIds(connection))
    }
}