package com.pissiphany.bany.mapper

import com.pissiphany.bany.adapter.dataStructure.YnabAccount
import com.pissiphany.bany.dataStructure.RetrofitAccount

class RetrofitAccountMapper {
    fun toYnabAccount(account: RetrofitAccount): YnabAccount {
        return YnabAccount(
            id = account.id,
            name = account.name,
            balanceInMilliUnits = account.balance,
            closed = account.closed,
            type = account.type
        )
    }
}