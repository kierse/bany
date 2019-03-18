package com.pissiphany.bany.dataStructure

import java.time.OffsetDateTime

data class RetrofitTransaction(
    val id: String = null,
    val account_id: String,
    val date: OffsetDateTime, // in UTC
    val amount: Long 
)
