package com.pissiphany.bany.plugin.cibc.dataStructure

data class CibcTransactionWrapper(val transactions: List<CibcTransaction> = emptyList()) {
    data class CibcTransaction(
        val id: String,
        val accountId: String,
        val date: String,
        val descriptionLine1: String,
        val transactionDescription: String,
        val credit: String,
        val debit: String
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
