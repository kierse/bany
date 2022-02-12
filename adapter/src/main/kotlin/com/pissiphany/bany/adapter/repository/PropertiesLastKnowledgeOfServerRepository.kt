package com.pissiphany.bany.adapter.repository

import com.pissiphany.bany.domain.dataStructure.BudgetAccountIds
import com.pissiphany.bany.domain.repository.YnabLastKnowledgeOfServerRepository
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*

class PropertiesLastKnowledgeOfServerRepository(private val pathToProperties: File) : YnabLastKnowledgeOfServerRepository {
    private val properties = Properties()

    init {
        if (pathToProperties.exists()) {
            FileInputStream(pathToProperties).use { reader ->
                properties.load(reader)
            }
        } else {
            pathToProperties.createNewFile()
        }
    }

    override suspend fun getLastKnowledgeOfServer(budgetAccountIds: BudgetAccountIds): Int {
        return properties.getProperty(budgetAccountIds.accountId, "0").toInt()
    }

    override suspend fun saveLastKnowledgeOfServer(budgetAccountIds: BudgetAccountIds, lastKnowledgeOfServer: Int) {
        properties.setProperty(budgetAccountIds.accountId, lastKnowledgeOfServer.toString())
    }

    fun saveChanges() {
        FileOutputStream(pathToProperties).use { writer ->
            properties.store(writer, null)
        }
    }
}