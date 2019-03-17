package com.pissiphany.bany.plugin.cibc.dataStructure

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CibcTransactionWrapper(val transactions: List<CibcTransaction> = emptyList()) {
    @JsonClass(generateAdapter = true)
    data class CibcTransaction(
        val id: String,
        val accountId: String,
        val date: String,
        val descriptionLine1: String,
        val transactionDescription: String,
        val credit: String? = null,
        val debit: String? = null
//        val transactionType: CibcTransactionType
    )
//    enum class CibcTransactionType {
//        DEP, // deposit
//        XFR, // transfer
//        PAY, // payment
//        POS, // ???
//        INT, // interest
//        CHQ, // cheque
//        CRE  // credit
//    }
}
