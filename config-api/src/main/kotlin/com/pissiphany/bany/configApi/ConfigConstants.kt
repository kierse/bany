package com.pissiphany.bany.configApi

import java.io.File

object ConfigConstants {
    val CONFIG_DIR = File(System.getProperty("user.home"), ".bany")
    val CONFIG_FILE = File(CONFIG_DIR, "bany.config")
}