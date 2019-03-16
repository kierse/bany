package com.pissiphany.bany.plugin.cibc.environment

class CibcEnvironment : Environment {
    override val baseUrl = "https://api.ebanking.cibc.com"
    override val staticUrl = "$baseUrl/public/66b6b4bfb218b5ab63ab8a0b4633c"
    override val appConfigUrl: String = "$baseUrl/ebm-resources/public/client/mobile/conf/appconfig-cibc.json"
    override val authUrl = "$baseUrl/ebm-anp/api/v1/json/sessions"
    override val accountsUrl = "$baseUrl/ebm-ai/api/v2/json/accounts"
    override val transactionsUrl = "$baseUrl/ebm-ai/api/v1/json/transactions"
    override val refererUrl = "$baseUrl/ebm-resources/public/banking/cibc/client/web/index.html"

    override val host = "www.cibconline.cibc.com"
    override val brand = "cibc"
}