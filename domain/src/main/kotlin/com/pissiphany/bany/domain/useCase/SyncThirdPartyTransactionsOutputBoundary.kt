package com.pissiphany.bany.domain.useCase

import com.pissiphany.bany.domain.dataStructure.SyncTransactionsResult

interface SyncThirdPartyTransactionsOutputBoundary {
    fun present(results: List<SyncTransactionsResult>)
}