package com.pissiphany.bany.mapper

import com.pissiphany.bany.adapter.dataStructure.YnabBudget
import com.pissiphany.bany.dataStructure.RetrofitBudget

class RetrofitBudgetMapper {
    fun toYnabBudget(retrofitBudget: RetrofitBudget): YnabBudget {
        return YnabBudget(
            id = retrofitBudget.id,
            name = retrofitBudget.name,
            lastModifiedOn = retrofitBudget.last_modified_on
        )
    }
}