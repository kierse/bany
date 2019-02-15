package com.pissiphany.bany.adapter.config

import com.pissiphany.bany.adapter.json.DataEnvelopeFactory
import com.pissiphany.bany.adapter.json.LocalDateTimeAdapter
import com.squareup.moshi.Moshi
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

internal class BanyConfigTest {
    private val json = """
        {
            "ynab_api_token": "token",
            "plugins": [
                {
                    "type": "type",
                    "username": "username",
                    "password": "password",
                    "connections": [
                        {
                            "name": "name",
                            "third_party_account_id": "third_party_account_id",
                            "ynab_budget_id": "ynab_budget_id",
                            "ynab_account_id": "ynab_account_id"
                        }
                    ]
                }
            ]
        }
    """

    companion object {
        private lateinit var moshi: Moshi

        @BeforeAll
        @JvmStatic
        internal fun setup() {
            moshi = Moshi.Builder()
                .add(DataEnvelopeFactory())
                .add(LocalDateTimeAdapter())
                .build()
        }
    }

    @Test
    fun config() {
        val adapter = moshi.adapter(BanyConfig::class.java)

        val config = adapter.fromJson(json) ?: fail("Unable to load config file!")

        assertEquals(config.ynabApiToken, "token")
        assertEquals(config.plugins[0].type, "type")
        assertEquals(config.plugins[0].username, "username")
        assertEquals(config.plugins[0].password, "password")
        assertEquals(config.plugins[0].connections[0].name, "name")
        assertEquals(config.plugins[0].connections[0].thirdPartyAccountId, "third_party_account_id")
        assertEquals(config.plugins[0].connections[0].ynabAccountId, "ynab_account_id")
        assertEquals(config.plugins[0].connections[0].ynabBudgetId, "ynab_budget_id")
    }
}