package com.pissiphany.bany.domain.gateway

import com.pissiphany.bany.domain.dataStructure.Transaction
import java.time.LocalDate

interface ThirdPartyTransactionGateway {
    val accountId: String

    fun getNewTransactionSince(date: LocalDate?): List<Transaction>
}