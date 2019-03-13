package com.pissiphany.bany.config

import com.squareup.moshi.Moshi
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

internal class BanyConfigTest {

    companion object {
        private lateinit var config: BanyConfig

        private val json = """
            {
                "ynab_api_token": "token",
                "plugins": {
                    "type": [
                        {
                            "username": "username",
                            "password": "password",
                            "enabled": false,
                            "description": "description",
                            "connections": [
                                {
                                    "name": "name",
                                    "third_party_account_id": "third_party_account_id",
                                    "ynab_budget_id": "ynab_budget_id",
                                    "ynab_account_id": "ynab_account_id"
                                },
                                {
                                    "name": "name2",
                                    "third_party_account_id": "third_party_account_id2",
                                    "ynab_budget_id": "ynab_budget_id2",
                                    "ynab_account_id": "ynab_account_id2"
                                }
                            ]
                        },
                        {
                            "username": "username2",
                            "password": "password2",
                            "connections": []
                        }
                    ]
                }
            }
        """.trimIndent()

        @BeforeAll
        @JvmStatic
        internal fun setup() {
            val moshi = Moshi.Builder().build()
            val adapter = moshi.adapter(BanyConfig::class.java)

            config = adapter.fromJson(json) ?: fail("Unable to load config file!")
        }
    }

    @Test
    fun config__ynab_api_token() {
        assertEquals("token", config.ynabApiToken)
    }

    @Test
    fun config__plugins() {
        assertNotNull(config.plugins.getValue("type"))
    }

    @Test
    fun config__username1() {
        assertEquals("username", config.plugins.getValue("type").first().username)
    }

    @Test
    fun config__password1() {
        assertEquals("password", config.plugins.getValue("type").first().password)
    }

    @Test
    fun config__enabled1() {
        assertFalse(config.plugins.getValue("type").first().enabled)
    }

    @Test
    fun config__description1() {
        assertEquals("description", config.plugins.getValue("type").first().description)
    }

    @Test
    fun config__connection1_name() {
        assertEquals("name", config.plugins.getValue("type").first().connections[0].name)
    }

    @Test
    fun config__connection1_third_party_account_id() {
        assertEquals("third_party_account_id", config.plugins.getValue("type").first().connections[0].thirdPartyAccountId)
    }

    @Test
    fun config__connection1_ynab_account_id() {
        assertEquals("ynab_account_id", config.plugins.getValue("type").first().connections[0].ynabAccountId)
    }

    @Test
    fun config__connection1_ynab_budget_it() {
        assertEquals("ynab_budget_id", config.plugins.getValue("type").first().connections[0].ynabBudgetId)
    }

    @Test
    fun config__connection2_exists() {
        assertEquals(2, config.plugins.getValue("type").size)
    }

    @Test
    fun config__enabled2_default_value() {
        assertEquals(true, config.plugins.getValue("type")[1].enabled)
    }
}