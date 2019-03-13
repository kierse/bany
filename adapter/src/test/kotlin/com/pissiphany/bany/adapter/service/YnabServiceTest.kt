package com.pissiphany.bany.adapter.service

import com.pissiphany.bany.adapter.BASE_URL
import com.pissiphany.bany.adapter.Constants.CONFIG_FILE
import com.pissiphany.bany.adapter.INTEGRATION_TEST
import com.pissiphany.bany.adapter.SLOW
import com.pissiphany.bany.config.BanyConfig
import com.pissiphany.bany.adapter.json.DataEnvelopeFactory
import com.pissiphany.bany.adapter.json.LocalDateTimeAdapter
import com.squareup.moshi.Moshi
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

// See https://spin.atomicobject.com/2018/07/18/gradle-integration-tests/
class YnabServiceTest {
    companion object {
        private lateinit var service: YnabService

        @BeforeAll
        @JvmStatic
        internal fun setup() {
            val moshi = Moshi.Builder()
                .add(DataEnvelopeFactory())
                .add(LocalDateTimeAdapter())
                .build()
            val adapter = moshi.adapter(BanyConfig::class.java)
            val config = adapter.fromJson(CONFIG_FILE.readText()) ?: throw UnknownError("unable to create config file!")

            val retrofit = RetrofitFactory.create(BASE_URL, config.ynabApiToken, moshi)

            service = retrofit.create(YnabService::class.java)
        }
    }

    @Tags(Tag(SLOW), Tag(INTEGRATION_TEST))
    @Test
    fun getBudgets() {
        val call = service.getBudgets()
        val response = call.execute()
        val body = response.body()

        assertTrue(response.isSuccessful)
        assertTrue(body?.budgets?.isNotEmpty() ?: false)
    }

    @Tags(Tag(SLOW), Tag(INTEGRATION_TEST))
    @Test
    fun getAccounts() {
        val budgetsCall = service.getBudgets()
        val budgetsResponse = budgetsCall.execute()
        val budget = budgetsResponse.body()?.budgets?.first() ?: fail("Unable to retrieve budgets")

        val accountsCall = service.getAccounts(budget.id)
        val accountsResponse = accountsCall.execute()
        val body = accountsResponse.body()

        assertTrue(accountsResponse.isSuccessful)
        assertTrue(body?.accounts?.isNotEmpty() ?: false)
    }

    @Tags(Tag(SLOW), Tag(INTEGRATION_TEST))
    @Test
    fun getAccounts__unknown_budget() {
        val accountsCall = service.getAccounts("fake-budget-id")
        val accountsResponse = accountsCall.execute()

        assertFalse(accountsResponse.isSuccessful)
        assertEquals(404, accountsResponse.code())
    }

//    @Test
//    fun test() {
//        val json = """
//            {
//    "data": {
//        "budget": {
//            "id": "54c56821-3099-498b-ae2c-96a3cee85907",
//            "name": "Bany testing",
//            "last_modified_on": "2019-02-20T08:01:47+00:00",
//            "date_format": {
//                "format": "YYYY/MM/DD"
//            },
//            "currency_format": {
//                "iso_code": "CAD",
//                "example_format": "123,456.78",
//                "decimal_digits": 2,
//                "decimal_separator": ".",
//                "symbol_first": true,
//                "group_separator": ",",
//                "currency_symbol": "${'$'}",
//                "display_symbol": true
//            },
//            "first_month": "2019-02-01",
//            "last_month": "2019-02-01",
//            "accounts": [],
//            "payees": [],
//            "payee_locations": [],
//            "category_groups": [],
//            "categories": [],
//            "months": [],
//            "transactions": [],
//            "subtransactions": [],
//            "scheduled_transactions": [],
//            "scheduled_subtransactions": []
//        },
//        "server_knowledge": 45
//    }
//}
//        """.trimIndent()
//
//
//        val moshi = Moshi.Builder()
//            .add(DataEnvelopeFactory())
//            .add(LocalDateTimeAdapter())
//            .build()
//        val adapter = moshi.adapter(YnabBudget::class.java)
//
//        val budget = adapter.fromJson(json)
//    }
}