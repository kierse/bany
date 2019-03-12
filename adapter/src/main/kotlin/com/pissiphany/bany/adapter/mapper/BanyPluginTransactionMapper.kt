package com.pissiphany.bany.adapter.mapper

import com.pissiphany.bany.plugin.dataStructure.BanyPluginTransaction
import com.pissiphany.bany.domain.dataStructure.Transaction

class BanyPluginTransactionMapper {
    fun toTransaction(pluginTransaction: BanyPluginTransaction): Transaction {
        return Transaction(pluginTransaction.id, pluginTransaction.date, pluginTransaction.amount)
    }
}