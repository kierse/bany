package com.pissiphany.bany.adapter.controller

import com.pissiphany.bany.domain.useCase.SyncThirdPartyTransactionsUseCase

class SyncTransactionsWithYnabController(
    private val syncTransactionsUseCase: SyncThirdPartyTransactionsUseCase
) {
    fun sync() {
        syncTransactionsUseCase.sync()
    }
}