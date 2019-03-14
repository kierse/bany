package com.pissiphany.bany.adapter.mapper

import com.pissiphany.bany.adapter.dataStructure.YnabAccount
import com.pissiphany.bany.domain.dataStructure.Account
import java.lang.IllegalArgumentException

class YnabAccountMapper {
    fun toAccount(ynabAccount: YnabAccount): Account {
        val type: Account.Type = when (ynabAccount.type) {
            "checking" ->          Account.Type.CHECKING
            "savings" ->           Account.Type.SAVINGS
            "cash" ->              Account.Type.CASH
            "creditCard" ->        Account.Type.CREDIT_CARD
            "lineOfCredit" ->      Account.Type.LINE_OF_CREDIT
            "otherAsset" ->        Account.Type.OTHER_ASSET
            "otherLiability" ->    Account.Type.OTHER_LIABILITY
            "payPal" ->            Account.Type.PAY_PAL
            "merchantAccount" ->   Account.Type.MERCHANT_ACCOUNT
            "investmentAccount" -> Account.Type.INVESTMENT_ACCOUNT
            "mortgage" ->         Account.Type.MORTGAGE
            else ->                throw IllegalArgumentException("unknown type: ${ynabAccount.type}")
        }

        return Account(
            id = ynabAccount.id,
            name = ynabAccount.name,
            balance = ynabAccount.balance,
            closed = ynabAccount.closed,
            type = type
        )
    }

    fun toYnabAccount(account: Account): YnabAccount {
        return YnabAccount(
            id = account.id,
            name = account.name,
            closed = account.closed,
            balance = account.balance,
            type = account.type.raw
        )
    }
}