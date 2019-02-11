package com.pissiphany.bany.domain.dataStructure

data class Account(val id: String, val name: String, val type: Type) {
    enum class Type {
        CASH,
        CHECKING,
        CREDIT_CARD,
        LINE_OF_CREDIT,
        OTHER_ASSET,
        OTHER_LIABILITY,
        SAVINGS,

        // deprecated
        INVESTMENT_ACCOUNT,
        MERCHANT_ACCOUNT,
        MORTGAGE,
        PAY_PAL
    }
}