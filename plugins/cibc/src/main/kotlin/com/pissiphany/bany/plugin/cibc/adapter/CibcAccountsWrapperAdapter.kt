package com.pissiphany.bany.plugin.cibc.adapter

import com.pissiphany.bany.plugin.cibc.dataStructure.CibcAccountsWrapper
import com.pissiphany.bany.plugin.cibc.dataStructure.SimpliiAccountsWrapper
import com.squareup.moshi.FromJson

internal class CibcAccountsWrapperAdapter {
    @FromJson
    fun cibcAccountFromSimpliiAccount(simpliiWrapper: SimpliiAccountsWrapper): CibcAccountsWrapper {
        val accounts = simpliiWrapper
            .accounts
            .map { simplii ->
                CibcAccountsWrapper.CibcAccount(
                    id = simplii.id,
                    number = simplii.number,
                    balance = simplii.balance.amount,
                    currency = simplii.balance.currency
                )
            }

        return CibcAccountsWrapper(accounts)
    }
}