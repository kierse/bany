package com.pissiphany.bany.service

import com.pissiphany.bany.BASE_URL
import com.pissiphany.bany.Constants.CONFIG_FILE
import com.pissiphany.bany.dataStructure.BanyConfig
import com.pissiphany.bany.factory.DataEnvelopeFactory
import com.pissiphany.bany.adapter.OffsetDateTimeAdapter
import com.pissiphany.bany.factory.RetrofitFactory
import com.squareup.moshi.Moshi
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

// See https://spin.atomicobject.com/2018/07/18/gradle-integration-tests/
class RetrofitYnabServiceTest {
    companion object {
        private lateinit var service: RetrofitYnabService

        @BeforeAll
        @JvmStatic
        internal fun setup() {
            val moshi = Moshi.Builder()
                .add(DataEnvelopeFactory())
                .add(OffsetDateTimeAdapter())
                .build()
            val adapter = moshi.adapter(BanyConfig::class.java)
            val config = adapter.fromJson(CONFIG_FILE.readText()) ?: throw UnknownError("unable to read config file!")

            val retrofit = RetrofitFactory.create(BASE_URL, config.ynabApiToken, moshi)

            service = retrofit.create(RetrofitYnabService::class.java)
        }
    }

    @Test
    fun getBudgets() {
        val call = service.getBudgets()
        val response = call.execute()
        val body = response.body()

        assertTrue(response.isSuccessful)
        assertTrue(body?.budgets?.isNotEmpty() ?: false)
    }

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

    @Test
    fun getAccounts__unknown_budget() {
        val accountsCall = service.getAccounts("fake-budget-id")
        val accountsResponse = accountsCall.execute()

        assertFalse(accountsResponse.isSuccessful)
        assertEquals(404, accountsResponse.code())
    }
}