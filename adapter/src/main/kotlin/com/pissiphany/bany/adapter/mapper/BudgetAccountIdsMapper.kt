package com.pissiphany.bany.adapter.mapper

import com.pissiphany.bany.adapter.config.Connection
import com.pissiphany.bany.domain.dataStructure.BudgetAccountIds

class BudgetAccountIdsMapper {
    fun toBudgetAccountIds(connection: Connection): BudgetAccountIds {
        return BudgetAccountIds(
            budgetId = connection.ynabBudgetId, accountId = connection.ynabAccountId
        )
    }
}