package com.pissiphany.bany.adapter.mapper

import com.pissiphany.bany.config.BanyConfigConnection
import com.pissiphany.bany.domain.dataStructure.BudgetAccountIds

class BudgetAccountIdsMapper {
    fun toBudgetAccountIds(connection: BanyConfigConnection): BudgetAccountIds {
        return BudgetAccountIds(
            budgetId = connection.ynabBudgetId, accountId = connection.ynabAccountId
        )
    }
}