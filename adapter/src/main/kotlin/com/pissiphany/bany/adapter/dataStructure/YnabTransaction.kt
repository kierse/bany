package com.pissiphany.bany.adapter.dataStructure

import java.time.LocalDate

data class YnabTransaction(val id: String, val accountId: String, val amount: Long, val date: LocalDate)
