package com.pissiphany.bany

import com.pissiphany.bany.configApi.ConfigConstants
import java.io.File

const val BASE_URL = "https://api.youneedabudget.com/"

object Constants {
    val LAST_KNOWLEDGE_OF_SERVER_FILE = File(ConfigConstants.CONFIG_DIR, "last_knowledge_of_server.properties")
}