package com.pissiphany.bany.adapter.mapper

import com.pissiphany.bany.adapter.dataStructure.YnabTransaction
import com.pissiphany.bany.plugin.dataStructure.BanyPluginTransaction
import java.math.BigDecimal

class BanyPluginTransactionMapper {
    fun toYnabTransaction(
        banyPluginTransaction: BanyPluginTransaction, accountId: String
    ): YnabTransaction {
        if (banyPluginTransaction.debit != BigDecimal.ZERO
            && banyPluginTransaction.credit != BigDecimal.ZERO)
            throw IllegalArgumentException("transaction has non-zero value for credit and debit")

        val amount = if (banyPluginTransaction.debit != BigDecimal.ZERO) {
            banyPluginTransaction.debit.negate()
        } else {
            banyPluginTransaction.credit
        }

        return YnabTransaction(
            // Note: coming from third party so no transaction id
            id = null,

            accountId = accountId,
            date = banyPluginTransaction.date,
            payee = banyPluginTransaction.payee,
            memo = banyPluginTransaction.memo,
            amountInMilliUnits = amount.movePointRight(3).toInt()
        )
    }
}