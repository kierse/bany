package com.pissiphany.bany.plugin.cibc.environment

class CibcEnvironment : Environment {
    override val baseUrl = "https://api.ebanking.cibc.com"
    override val appConfigUrl: String = "$baseUrl/ebm-resources/public/client/mobile/conf/appconfig-cibc.json"
    override val authUrl = "$baseUrl/ebm-anp/api/v1/json/sessions"
    override val accountsUrl = "$baseUrl/ebm-ai/api/v2/json/accounts"
    override val transactionsUrl = "$baseUrl/ebm-ai/api/v1/json/transactions"
    override val brand = "cibc"
}