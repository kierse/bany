package com.pissiphany.bany.adapter

import java.io.File

object Constants {
    val CONFIG_DIR = File(System.getProperty("user.home"), ".bany")
    val CONFIG_FILE = File(CONFIG_DIR, "bany.config")
}