package com.pissiphany.bany.adapter.repository

import com.pissiphany.bany.domain.dataStructure.BudgetAccountIds
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
class PropertiesLastKnowledgeOfServerRepositoryTest {
    @Test
    fun `getLastKnowledgeOfServer - empty file`() = runTest {
        val ids = BudgetAccountIds("name_1", "budget_id_1", "account_id_1")

        val nonExistentFile = File(System.getProperty("java.io.tmpdir"), "foo-bar.tmp")
        val repo = PropertiesLastKnowledgeOfServerRepository(this, nonExistentFile)

        assertTrue(repo.isEmpty())
        assertTrue(nonExistentFile.exists())
        assertEquals(0, repo.getLastKnowledgeOfServer(ids))

        nonExistentFile.delete()
    }

    @Test
    fun `getLastKnowledgeOfServer - file exists`() = runTest {
        val ids = BudgetAccountIds("name_1", "budget_id_1", "account_id_1")

        val file = withContext(Dispatchers.IO) {
            createPropFile("getLastKnowledgeOfServer__file_exists", mapOf("account_id_1" to 123, "account_id_2" to 5))
        }
        val repo = PropertiesLastKnowledgeOfServerRepository(this, file)

        assertEquals(123, repo.getLastKnowledgeOfServer(ids))

        file.delete()
    }

    @Test
    fun saveLastKnowledgeOfServer() = runTest {
        val ids = BudgetAccountIds("name_1", "budget_id_1", "account_id_2")

        val file = withContext(Dispatchers.IO) {
            createPropFile("saveLastKnowledgeOfServer",mapOf("account_id_1" to 123, "account_id_2" to 5))
        }
        val repo = PropertiesLastKnowledgeOfServerRepository(this, file)
        file.delete()

        repo.saveLastKnowledgeOfServer(ids, 10)

        assertEquals(10, repo.getLastKnowledgeOfServer(ids))
    }

    @Test
    fun saveChanges() = runTest {
        val ids = BudgetAccountIds("name_1", "budget_id_1", "account_id_2")

        val file = withContext(Dispatchers.IO) { File.createTempFile("saveChanges", null) }
        val repo = PropertiesLastKnowledgeOfServerRepository(this, file)

        repo.saveLastKnowledgeOfServer(ids, 9)
        repo.saveChanges()

        val repo2 = PropertiesLastKnowledgeOfServerRepository(this, file)
        assertEquals(9, repo2.getLastKnowledgeOfServer(ids))

        file.delete()
    }

    private suspend fun createPropFile(name: String, data: Map<String, Int>) = withContext(Dispatchers.IO) {
        val properties = Properties()
        data.forEach { (key, value) -> properties.setProperty(key, value.toString()) }

        File.createTempFile(name, null)
            .apply {
                writer().use { writer -> properties.store(writer, null) }
            }
    }
}