package com.pissiphany.bany.plugin.cibc.mapper

import com.pissiphany.bany.plugin.BanyPluginTransaction
import com.pissiphany.bany.plugin.cibc.dataStructure.CibcTransactionWrapper
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class CibcTransactionMapper {
    fun toBanyPluginTransaction(cibcTransaction: CibcTransactionWrapper.CibcTransaction): BanyPluginTransaction {
        return BanyPluginTransaction(
            id = cibcTransaction.id,
            date = LocalDate.parse(cibcTransaction.date, DateTimeFormatter.ISO_LOCAL_DATE),
            amount = 0L // TODO find transaction value
        )
    }
}