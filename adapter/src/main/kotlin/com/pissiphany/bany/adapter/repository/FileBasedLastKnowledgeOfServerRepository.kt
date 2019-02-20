package com.pissiphany.bany.adapter.repository

import com.pissiphany.bany.domain.dataStructure.Account
import com.pissiphany.bany.domain.repository.YnabLastKnowledgeOfServerRepository
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*

class FileBasedLastKnowledgeOfServerRepository(private val pathToProperties: File) : YnabLastKnowledgeOfServerRepository {
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

    override fun getLastKnowledgeOfServer(account: Account): Int {
        return properties.getProperty(account.id, "0").toInt()
    }

    override fun saveLastKnowledgeOfServer(account: Account, lastKnowledgeOfServer: Int) {
        properties.setProperty(account.id, lastKnowledgeOfServer.toString())
    }

    internal fun saveChanges() {
        FileOutputStream(pathToProperties).use { writer ->
            properties.store(writer, null)
        }
    }
}