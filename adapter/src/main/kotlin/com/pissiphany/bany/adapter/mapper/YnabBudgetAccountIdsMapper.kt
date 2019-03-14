package com.pissiphany.bany.adapter.mapper

import com.pissiphany.bany.adapter.dataStructure.YnabConnection
import com.pissiphany.bany.domain.dataStructure.BudgetAccountIds

class YnabBudgetAccountIdsMapper {
    fun toBudgetAccountIds(connection: YnabConnection): BudgetAccountIds {
        return BudgetAccountIds(
            budgetId = connection.ynabBudgetId, accountId = connection.ynabAccountId
        )
    }
}