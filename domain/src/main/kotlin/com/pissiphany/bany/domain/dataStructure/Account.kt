package com.pissiphany.bany.domain.dataStructure

data class Account(val id: String, val name: String, val balance: Long, val closed: Boolean, val type: Type) {
    enum class Type(val raw: String) {
        CASH("cash"),
        CHECKING("checking"),
        CREDIT_CARD("creditCard"),
        LINE_OF_CREDIT("lineOfCredit"),
        OTHER_ASSET("otherAsset"),
        OTHER_LIABILITY("otherLiability"),
        SAVINGS("savings"),

        // deprecated
        INVESTMENT_ACCOUNT("investmentAccount"),
        MERCHANT_ACCOUNT("merchantAccount"),
        MORTGAGE("mortgage"),
        PAY_PAL("payPal")
    }
}