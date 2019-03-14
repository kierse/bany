package com.pissiphany.bany.dataStructure

import java.time.LocalDateTime

data class RetrofitAccountsWrapper(
    val accounts: List<RetrofitAccount>, val last_modified_on: LocalDateTime
)