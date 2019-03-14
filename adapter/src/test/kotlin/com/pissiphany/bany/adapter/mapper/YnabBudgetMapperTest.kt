package com.pissiphany.bany.adapter.mapper

import com.pissiphany.bany.adapter.dataStructure.YnabBudget
import com.pissiphany.bany.domain.dataStructure.Budget
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.time.LocalDateTime

internal class YnabBudgetMapperTest {
    @Test
    fun toBudget() {
        val ynabBudget = YnabBudget("id", "name", LocalDateTime.now())
        assertEquals(Budget("id", "name"), YnabBudgetMapper().toBudget(ynabBudget))
    }
}