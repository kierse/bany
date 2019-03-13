package com.pissiphany.bany.plugin.cibc.environment

interface Environment {
    val name: String

    val baseUrl: String
    val staticUrl: String
    val authUrl: String
    val accountsUrl: String
    val transactionsUrl: String
    val refererUrl: String

    val host: String
    val brand: String
}