package com.pissiphany.bany.dataStructure

import com.squareup.moshi.JsonClass
import java.time.LocalDate

@JsonClass(generateAdapter = true)
data class RetrofitTransaction(
    val id: String? = null,
    val account_id: String,
    val date: LocalDate,
    val payee_name: String?,
    val memo: String?,
    val amount: Int // in milli-units (ie $123.90 => 123900)
)
