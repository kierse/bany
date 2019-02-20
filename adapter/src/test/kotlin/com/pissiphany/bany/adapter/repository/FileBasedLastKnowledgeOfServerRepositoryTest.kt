package com.pissiphany.bany.adapter.repository

import com.pissiphany.bany.domain.dataStructure.Account
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.io.File
import java.io.FileOutputStream
import java.util.*

internal class FileBasedLastKnowledgeOfServerRepositoryTest {
    @Test
    fun getLastKnowledgeOfServer__empty_file() {
        val account = Account("account_id_1", "name", 0L, false, Account.Type.CHECKING)

        val nonExistentFile = File(System.getProperty("java.io.tmpdir"), "foo-bar.tmp")
        val repo = FileBasedLastKnowledgeOfServerRepository(nonExistentFile)

        assertTrue(nonExistentFile.exists())
        assertEquals(0, repo.getLastKnowledgeOfServer(account))

        nonExistentFile.delete()
    }

    @Test
    fun getLastKnowledgeOfServer__file_exists() {
        val account = Account("account_id_1", "name", 0L, false, Account.Type.CHECKING)

        val file = File.createTempFile("getLastKnowledgeOfServer__file_exists", null)
        createPropFile(file, mapOf("account_id_1" to 123, "account_id_2" to 5))
        val repo = FileBasedLastKnowledgeOfServerRepository(file)
        file.delete()

        assertEquals(123, repo.getLastKnowledgeOfServer(account))
    }

    @Test
    fun saveLastKnowledgeOfServer() {
        val account = Account("account_id_2", "name", 0L, false, Account.Type.CHECKING)

        val file = File.createTempFile("saveLastKnowledgeOfServer", null)
        createPropFile(file, mapOf("account_id_1" to 123, "account_id_2" to 5))
        val repo = FileBasedLastKnowledgeOfServerRepository(file)
        file.delete()

        repo.saveLastKnowledgeOfServer(account, 10)

        assertEquals(10, repo.getLastKnowledgeOfServer(account))
    }

    @Test
    fun saveChanges() {
        val account = Account("account_id_2", "name", 0L, false, Account.Type.CHECKING)

        val file = File.createTempFile("saveChanges", null)
        val repo = FileBasedLastKnowledgeOfServerRepository(file)

        repo.saveLastKnowledgeOfServer(account, 9)
        repo.saveChanges()

        val repo2 = FileBasedLastKnowledgeOfServerRepository(file)

        assertEquals(9, repo2.getLastKnowledgeOfServer(account))

        file.delete()
    }

    private fun createPropFile(path: File, data: Map<String, Int>) {
        val properties = Properties()
        data.forEach { key, value -> properties.setProperty(key, value.toString()) }
        FileOutputStream(path).use { writer ->
            properties.store(writer, null)
        }
    }
}