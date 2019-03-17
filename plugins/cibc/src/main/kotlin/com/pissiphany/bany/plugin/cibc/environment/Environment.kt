package com.pissiphany.bany.plugin.cibc.environment

interface Environment {
    val baseUrl: String
    val appConfigUrl: String
    val authUrl: String
    val accountsUrl: String
    val transactionsUrl: String
    val brand: String
}