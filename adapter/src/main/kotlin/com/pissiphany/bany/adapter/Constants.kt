package com.pissiphany.bany.adapter

import java.io.File

const val BASE_URL = "https://api.youneedabudget.com/"

object Constants {
    val CONFIG_DIR = File(System.getProperty("user.home"), ".bany")
    val CONFIG_FILE = File(CONFIG_DIR, "bany.config")
    val LAST_KNOWLEDGE_OF_SERVER_FILE = File(CONFIG_DIR, "last_knowledge_of_server.properties")
}