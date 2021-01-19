package com.pissiphany.bany.domain.gateway

import com.pissiphany.bany.domain.dataStructure.Account
import com.pissiphany.bany.domain.dataStructure.BudgetAccountIds
import com.pissiphany.bany.domain.dataStructure.UpdatedTransactions

interface YnabAccountDetailsGateway {
    fun getAccount(budgetAccountIds: BudgetAccountIds): Account?
    fun getUpdatedTransactions(budgetAccountIds: BudgetAccountIds, lastKnowledgeOfServer: Int): UpdatedTransactions
}