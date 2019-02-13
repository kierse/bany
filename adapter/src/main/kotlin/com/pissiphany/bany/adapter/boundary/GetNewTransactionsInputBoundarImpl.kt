package com.pissiphany.bany.adapter.boundary

import com.pissiphany.bany.domain.dataStructure.Account
import com.pissiphany.bany.domain.useCase.thirdPartyTransactions.GetNewTransactionsInputBoundary
import java.time.LocalTime

class GetNewTransactionsInputBoundarImpl(override val account: Account, override val date: LocalTime?) : GetNewTransactionsInputBoundary