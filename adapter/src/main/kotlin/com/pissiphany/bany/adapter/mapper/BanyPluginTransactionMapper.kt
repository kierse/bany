package com.pissiphany.bany.adapter.mapper

import com.pissiphany.bany.adapter.plugin.BanyPluginTransaction
import com.pissiphany.bany.domain.dataStructure.Transaction

class BanyPluginTransactionMapper {
    fun toTransaction(pluginTransaction: BanyPluginTransaction): Transaction {
        return Transaction(pluginTransaction.id, pluginTransaction.date, pluginTransaction.amount)
    }
}