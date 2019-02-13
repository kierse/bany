package com.pissiphany.bany.domain.useCase.thirdPartyTransactions

import com.pissiphany.bany.domain.dataStructure.Account
import java.time.LocalTime

interface GetNewTransactionsInputBoundary {
    val account: Account
    val date: LocalTime?
}
