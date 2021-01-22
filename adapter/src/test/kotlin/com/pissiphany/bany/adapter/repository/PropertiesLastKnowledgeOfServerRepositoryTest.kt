package com.pissiphany.bany.adapter.repository

import com.pissiphany.bany.domain.dataStructure.BudgetAccountIds
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.io.File
import java.io.FileOutputStream
import java.util.*

internal class PropertiesLastKnowledgeOfServerRepositoryTest {
    @Test
    fun getLastKnowledgeOfServer__empty_file() {
        val ids = BudgetAccountIds("name_1", "budget_id_1", "account_id_1")

        val nonExistentFile = File(System.getProperty("java.io.tmpdir"), "foo-bar.tmp")
        val repo = PropertiesLastKnowledgeOfServerRepository(nonExistentFile)

        assertTrue(nonExistentFile.exists())
        assertEquals(0, repo.getLastKnowledgeOfServer(ids))

        nonExistentFile.delete()
    }

    @Test
    fun getLastKnowledgeOfServer__file_exists() {
        val ids = BudgetAccountIds("name_1", "budget_id_1", "account_id_1")

        val file = File.createTempFile("getLastKnowledgeOfServer__file_exists", null)
        createPropFile(file, mapOf("account_id_1" to 123, "account_id_2" to 5))
        val repo = PropertiesLastKnowledgeOfServerRepository(file)
        file.delete()

        assertEquals(123, repo.getLastKnowledgeOfServer(ids))
    }

    @Test
    fun saveLastKnowledgeOfServer() {
        val ids = BudgetAccountIds("name_1", "budget_id_1", "account_id_2")

        val file = File.createTempFile("saveLastKnowledgeOfServer", null)
        createPropFile(file, mapOf("account_id_1" to 123, "account_id_2" to 5))
        val repo = PropertiesLastKnowledgeOfServerRepository(file)
        file.delete()

        repo.saveLastKnowledgeOfServer(ids, 10)

        assertEquals(10, repo.getLastKnowledgeOfServer(ids))
    }

    @Test
    fun saveChanges() {
        val ids = BudgetAccountIds("name_1", "budget_id_1", "account_id_2")

        val file = File.createTempFile("saveChanges", null)
        val repo = PropertiesLastKnowledgeOfServerRepository(file)

        repo.saveLastKnowledgeOfServer(ids, 9)
        repo.saveChanges()

        val repo2 = PropertiesLastKnowledgeOfServerRepository(file)

        assertEquals(9, repo2.getLastKnowledgeOfServer(ids))

        file.delete()
    }

    private fun createPropFile(path: File, data: Map<String, Int>) {
        val properties = Properties()
        data.forEach { (key, value) -> properties.setProperty(key, value.toString()) }
        FileOutputStream(path).use { writer ->
            properties.store(writer, null)
        }
    }
}