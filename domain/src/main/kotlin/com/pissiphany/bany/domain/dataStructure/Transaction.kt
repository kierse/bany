package com.pissiphany.bany.domain.dataStructure

import java.time.LocalDate


data class Transaction(val id: String, val date: LocalDate, val amount: Long)