package com.pissiphany.bany.adapter.dataStructure

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class YnabBudgetsWrapper(val data: YnabBudgets)