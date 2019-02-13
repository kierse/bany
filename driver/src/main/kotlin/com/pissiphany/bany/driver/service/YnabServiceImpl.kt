package com.pissiphany.bany.driver.service

import com.pissiphany.bany.adapter.service.YnabService
import com.pissiphany.bany.domain.dataStructure.Account
import com.pissiphany.bany.domain.dataStructure.Budget
import com.pissiphany.bany.domain.dataStructure.Transaction
import java.time.LocalDate

class YnabServiceImpl: YnabService {
    override fun getTransactionsSince(budget: Budget, account: Account, since: LocalDate): List<Transaction> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getTransaction(budget: Budget, account: Account): List<Transaction> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}