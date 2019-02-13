package com.pissiphany.bany.adapter.service

import com.pissiphany.bany.domain.dataStructure.Account
import com.pissiphany.bany.domain.dataStructure.Budget
import com.pissiphany.bany.domain.dataStructure.Transaction
import java.time.LocalDate

interface YnabService {
    fun getTransactionsSince(budget: Budget, account: Account, since: LocalDate): List<Transaction>
    fun getTransaction(budget: Budget, account: Account): List<Transaction>
}