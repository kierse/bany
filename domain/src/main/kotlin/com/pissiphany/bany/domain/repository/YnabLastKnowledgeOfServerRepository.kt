package com.pissiphany.bany.domain.repository

import com.pissiphany.bany.domain.dataStructure.Account

interface YnabLastKnowledgeOfServerRepository {
    fun getLastKnowledgeOfServer(account: Account): Int
    fun saveLastKnowledgeOfServer(lastKnowledgeOfServer: Int)
}