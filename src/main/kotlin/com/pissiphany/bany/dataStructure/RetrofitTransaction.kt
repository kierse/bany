package com.pissiphany.bany.dataStructure

import java.time.LocalDate

data class RetrofitTransaction(
    val id: String, val account_id: String, val amount: Long, val date: LocalDate
)
