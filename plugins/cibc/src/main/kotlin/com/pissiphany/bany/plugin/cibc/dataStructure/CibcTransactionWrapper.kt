package com.pissiphany.bany.plugin.cibc.dataStructure

import com.squareup.moshi.JsonClass
import java.math.BigDecimal
import java.time.OffsetDateTime

@JsonClass(generateAdapter = true)
data class CibcTransactionWrapper(val transactions: List<CibcTransaction> = emptyList()) {
    @JsonClass(generateAdapter = true)
    data class CibcTransaction(
        val date: OffsetDateTime,
        val descriptionLine1: String,
        val transactionDescription: String,
        val credit: BigDecimal = BigDecimal.ZERO,
        val debit: BigDecimal = BigDecimal.ZERO
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
