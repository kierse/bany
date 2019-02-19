package com.pissiphany.bany.adapter.mapper

import com.pissiphany.bany.adapter.dataStructure.YnabBudget
import com.pissiphany.bany.domain.dataStructure.Budget

class BudgetMapper {
    fun toBudget(ynabBudget: YnabBudget): Budget {
        return Budget(
            id = ynabBudget.id,
            name = ynabBudget.name
        )
    }
}