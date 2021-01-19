package com.pissiphany.bany.adapter.mapper

import com.pissiphany.bany.adapter.dataStructure.YnabBudgetAccountIds
import com.pissiphany.bany.adapter.dataStructure.YnabConnection
import com.pissiphany.bany.domain.dataStructure.BudgetAccountIds

class YnabBudgetAccountIdsMapper {
    fun toBudgetAccountIds(connection: YnabConnection): BudgetAccountIds {
        return BudgetAccountIds(
            name = connection.name,
            budgetId = connection.ynabBudgetId,
            accountId = connection.ynabAccountId
        )
    }

    fun toYnabBudgetAccountIds(budgetAccountIds: BudgetAccountIds): YnabBudgetAccountIds {
        return YnabBudgetAccountIds(
            ynabBudgetId = budgetAccountIds.budgetId, ynabAccountId = budgetAccountIds.accountId
        )
    }
}