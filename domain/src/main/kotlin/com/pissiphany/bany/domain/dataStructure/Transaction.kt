package com.pissiphany.bany.domain.dataStructure

import java.time.LocalTime

data class Transaction(val id: String, val date: LocalTime, val amount: Long)