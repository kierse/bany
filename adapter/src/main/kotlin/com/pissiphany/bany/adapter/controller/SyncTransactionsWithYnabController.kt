package com.pissiphany.bany.adapter.controller

import com.pissiphany.bany.domain.useCase.GetAccountsUseCase
import com.pissiphany.bany.domain.useCase.GetBudgetsUseCase
import com.pissiphany.bany.domain.useCase.GetMostRecentTransactionUseCase

class SyncTransactionsWithYnabController(
    private val budgetsUseCase: GetBudgetsUseCase,
    private val accountsUseCase: GetAccountsUseCase,
    private val transactionUseCase: GetMostRecentTransactionUseCase
) {
    fun sync() {
        for (budget in budgetsUseCase.getBudgets()) {
            for (account in accountsUseCase.getActiveAccounts(budget)) {
                TODO("is any provider associated with this account??")

                val transaction = transactionUseCase.getTransaction(budget, account)
                TODO("call appropriate provider passing nullable transaction")
            }
        }
    }
}