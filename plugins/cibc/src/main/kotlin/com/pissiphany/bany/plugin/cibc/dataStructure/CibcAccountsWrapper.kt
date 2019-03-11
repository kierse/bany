package com.pissiphany.bany.plugin.cibc.dataStructure

data class CibcAccountsWrapper(val accounts: List<CibcAccount> = emptyList()) {
    data class CibcAccount(val id: String, val number: String, val balance: CibcAccountBalance)
    data class CibcAccountBalance(val currency: String, val amount: String)
}

