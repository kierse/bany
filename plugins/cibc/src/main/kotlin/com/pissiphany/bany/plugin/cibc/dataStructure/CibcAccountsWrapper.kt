package com.pissiphany.bany.plugin.cibc.dataStructure

data class CibcAccountsWrapper(val accounts: List<CibcAccount>) {
    data class CibcAccount(val id: String, val number: String, val balance: String, val currency: String)
}
