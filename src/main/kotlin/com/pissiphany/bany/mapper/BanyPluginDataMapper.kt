package com.pissiphany.bany.mapper

import com.pissiphany.bany.adapter.dataStructure.YnabTransaction
import com.pissiphany.bany.plugin.dataStructure.BanyPluginBudgetAccountIds
import com.pissiphany.bany.plugin.dataStructure.BanyPluginTransaction
import com.pissiphany.bany.plugin.dataStructure.YnabBudgetAccountIds
import java.math.BigDecimal

// TODO update tests!
class BanyPluginDataMapper {
    fun toYnabBudgetAccountIds(
        banyPluginBudgetAccountIds: BanyPluginBudgetAccountIds
    ): YnabBudgetAccountIds {
        return YnabBudgetAccountIds(
            ynabBudgetId =  banyPluginBudgetAccountIds.ynabBudgetId,
            ynabAccountId = banyPluginBudgetAccountIds.ynabAccountId
        )
    }

    fun toBanyPluginBudgetAccountIds(budgetAccountIds: YnabBudgetAccountIds): BanyPluginBudgetAccountIds {
        return BanyPluginBudgetAccountIds(
            ynabBudgetId = budgetAccountIds.ynabBudgetId,
            ynabAccountId = budgetAccountIds.ynabAccountId
        )
    }

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