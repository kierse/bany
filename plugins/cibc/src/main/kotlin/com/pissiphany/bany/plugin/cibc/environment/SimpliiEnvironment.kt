package com.pissiphany.bany.plugin.cibc.environment

class SimpliiEnvironment : Environment {
    override val baseUrl = "https://online.simplii.com"
    override val appConfigUrl: String = "$baseUrl/ebm-resources/public/client/mobile/conf/appconfig-pcf.json"
    override val authUrl = "$baseUrl/ebm-anp/api/v1/json/sessions"
    override val accountsUrl = "$baseUrl/ebm-ai/api/v2/json/accounts"
    override val transactionsUrl = "$baseUrl/ebm-ai/api/v1/json/transactions"
    override val brand = "simplii"
}