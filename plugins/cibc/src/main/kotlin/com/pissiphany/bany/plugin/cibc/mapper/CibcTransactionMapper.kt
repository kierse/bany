package com.pissiphany.bany.plugin.cibc.mapper

import com.pissiphany.bany.plugin.dataStructure.BanyPluginTransaction
import com.pissiphany.bany.plugin.cibc.dataStructure.CibcTransactionWrapper

class CibcTransactionMapper {
    fun toBanyPluginTransaction(cibcTransaction: CibcTransactionWrapper.CibcTransaction): BanyPluginTransaction {
        return BanyPluginTransaction(
            id = cibcTransaction.id,
            amount = 0L // TODO find transaction value
            date = cibcTransaction.date
        )
    }
}