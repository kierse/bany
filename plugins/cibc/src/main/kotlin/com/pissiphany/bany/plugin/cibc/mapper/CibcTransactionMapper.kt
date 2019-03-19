package com.pissiphany.bany.plugin.cibc.mapper

import com.pissiphany.bany.plugin.dataStructure.BanyPluginTransaction
import com.pissiphany.bany.plugin.cibc.dataStructure.CibcTransactionWrapper

class CibcTransactionMapper {
    fun toBanyPluginTransaction(cibcTransaction: CibcTransactionWrapper.CibcTransaction): BanyPluginTransaction {
        return BanyPluginTransaction(
            date = cibcTransaction.date,
            payee = cibcTransaction.descriptionLine1,
            memo = cibcTransaction.transactionDescription,
            debit = cibcTransaction.debit,
            credit = cibcTransaction.credit
        )
    }
}