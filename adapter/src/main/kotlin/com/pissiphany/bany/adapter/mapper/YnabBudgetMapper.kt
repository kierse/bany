package com.pissiphany.bany.adapter.mapper

import com.pissiphany.bany.adapter.dataStructure.YnabBudget
import com.pissiphany.bany.domain.dataStructure.Budget
import java.time.LocalDateTime

class YnabBudgetMapper {
    fun toBudget(ynabBudget: YnabBudget): Budget {
        return Budget(
            id = ynabBudget.id,
            name = ynabBudget.name
        )
    }

    fun toYnabBudget(budget: Budget): YnabBudget {
        return YnabBudget(
            id = budget.id,
            name = budget.name,
            lastModifiedOn = LocalDateTime.now() // TODO ???
        )
    }
}