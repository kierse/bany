package com.pissiphany.bany.adapter.mapper

import com.pissiphany.bany.config.BanyConnection
import com.pissiphany.bany.domain.dataStructure.BudgetAccountIds

class BudgetAccountIdsMapper {
    fun toBudgetAccountIds(connection: BanyConnection): BudgetAccountIds {
        return BudgetAccountIds(
            budgetId = connection.ynabBudgetId, accountId = connection.ynabAccountId
        )
    }
}