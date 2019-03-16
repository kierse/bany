package com.pissiphany.bany.plugin.cibc.dataStructure

internal data class SimpliiAccountsWrapper(val accounts: List<SimpliiAccount>) {
    data class SimpliiAccount(val id: String, val number: String, val balance: SimpliiAccountBalance)
    data class SimpliiAccountBalance(val currency: String, val amount: String)
}

