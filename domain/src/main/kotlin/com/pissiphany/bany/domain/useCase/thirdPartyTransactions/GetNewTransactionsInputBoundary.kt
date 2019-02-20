package com.pissiphany.bany.domain.useCase.thirdPartyTransactions

import com.pissiphany.bany.domain.dataStructure.Account
import java.time.LocalDate

interface GetNewTransactionsInputBoundary {
    val account: Account
    val date: LocalDate?
}
