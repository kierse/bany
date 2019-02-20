package com.pissiphany.bany.domain.gateway

import com.pissiphany.bany.domain.dataStructure.Account
import com.pissiphany.bany.domain.dataStructure.Transaction
import java.time.LocalDate
import java.time.LocalTime

interface ThirdPartyTransactionGateway {
    val account: Account

    fun getNewTransactionSince(date: LocalDate?): List<Transaction>
}