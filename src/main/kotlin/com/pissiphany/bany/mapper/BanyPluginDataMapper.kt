package com.pissiphany.bany.mapper

import com.pissiphany.bany.adapter.dataStructure.YnabAccountBalance
import com.pissiphany.bany.adapter.dataStructure.YnabAccountTransaction
import com.pissiphany.bany.adapter.dataStructure.YnabTransaction
import com.pissiphany.bany.plugin.dataStructure.BanyPluginBudgetAccountIds
import com.pissiphany.bany.plugin.dataStructure.BanyPluginTransaction
import com.pissiphany.bany.adapter.dataStructure.YnabBudgetAccountIds
import com.pissiphany.bany.plugin.dataStructure.BanyPluginAccountBalance
import com.pissiphany.bany.plugin.dataStructure.BanyPluginAccountTransaction

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
        return when(banyPluginTransaction) {
            is BanyPluginAccountTransaction -> toYnabAccountTransaction(banyPluginTransaction, accountId)
            is BanyPluginAccountBalance -> toYnabAccountBalance(banyPluginTransaction, accountId)
        }
    }

    private fun toYnabAccountTransaction(
        accountTransaction: BanyPluginAccountTransaction, accountId: String
    ): YnabAccountTransaction {
        return YnabAccountTransaction(
            id = null,
            memo = accountTransaction.memo,
            accountId = accountId,
            date = accountTransaction.date,
            payee = accountTransaction.payee,
            amountInMilliUnits = accountTransaction.amount.movePointRight(3).toInt()
        )
    }

    private fun toYnabAccountBalance(accountBalance: BanyPluginAccountBalance, accountId: String): YnabAccountBalance {
        return YnabAccountBalance(
            accountId = accountId,
            date = accountBalance.date,
            payee = accountBalance.payee,
            amountInMilliUnits = accountBalance.amount.movePointRight(3).toInt()
        )
    }
}