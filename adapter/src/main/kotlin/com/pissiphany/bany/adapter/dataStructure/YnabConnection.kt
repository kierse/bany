package com.pissiphany.bany.adapter.dataStructure

import com.pissiphany.bany.plugin.BanyPlugin

data class YnabConnection(
    val name: String,
    override val ynabBudgetId: String,
    override val ynabAccountId: String,
    override val thirdPartyAccountId: String,
    override val data: Map<String, String> = emptyMap()
) : BanyPlugin.Connection