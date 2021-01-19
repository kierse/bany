package com.pissiphany.bany.domain.useCase.step

import com.pissiphany.bany.domain.dataStructure.*
import com.pissiphany.bany.domain.useCase.SyncThirdPartyTransactionsUseCase

class ProcessNewTransaction : SyncThirdPartyTransactionsUseCase.Step3ProcessNewTransaction {
    override fun processTransaction(account: Account, newTransaction: Transaction): AccountTransaction {
        return when(newTransaction) {
            is AccountBalance -> processNewBalanceTransaction(account, newTransaction)
            is AccountTransaction -> newTransaction
        }
    }

    private fun processNewBalanceTransaction(
        account: Account, accountBalance: AccountBalance
    ): AccountTransaction {
        val differenceInCents = accountBalance.amountInCents - account.balanceInCents
        return AccountTransaction(
            id = null,
            date = accountBalance.date,
            payee = accountBalance.payee,
            memo = "",
            amountInCents = differenceInCents
        )
    }
}