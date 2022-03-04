package com.pissiphany.bany.adapter.repository

import com.pissiphany.bany.domain.dataStructure.BudgetAccountIds
import com.pissiphany.bany.domain.repository.YnabLastKnowledgeOfServerRepository
import kotlinx.coroutines.*
import java.io.File
import java.util.*

class PropertiesLastKnowledgeOfServerRepository(
    coroutineScope: CoroutineScope,
    private val pathToProperties: File,
) : YnabLastKnowledgeOfServerRepository {
    private val properties = coroutineScope.async(Dispatchers.IO) {
        Properties().apply {
            if (pathToProperties.exists()) {
                pathToProperties.reader().use(::load)
            } else {
                pathToProperties.createNewFile()
            }
        }
    }

    internal suspend fun isEmpty() = properties.await().isEmpty

    override suspend fun getLastKnowledgeOfServer(budgetAccountIds: BudgetAccountIds): Int {
        return properties.await().getProperty(budgetAccountIds.accountId, "0").toInt()
    }

    override suspend fun saveLastKnowledgeOfServer(budgetAccountIds: BudgetAccountIds, lastKnowledgeOfServer: Int) {
        properties.await().setProperty(budgetAccountIds.accountId, lastKnowledgeOfServer.toString())
    }

    suspend fun saveChanges() = withContext(Dispatchers.IO) {
        pathToProperties.writer().use { writer -> properties.await().store(writer, null) }
    }
}