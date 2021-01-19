package com.pissiphany.bany.adapter.view

import com.pissiphany.bany.adapter.dataStructure.ViewModel
import com.pissiphany.bany.adapter.presenter.Presenter

class ConsoleView : Presenter.View {
    override fun display(model: ViewModel) {
        for (record in model.records) {
            println()
            println(record.syncIndex)
            println(record.budgetId)
            println(record.accountId)
            println(record.accountName)
            println()

            if (record.latestTransactionDate.isNotBlank()) {
                println(record.latestTransactionDate)
            }

            println(record.numberOfTransactionsFound)

            for (transaction in record.transactions) {
                println(transaction)
            }

            println()
        }
    }
}