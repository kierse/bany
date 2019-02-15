package com.pissiphany.bany.adapter.service

import com.pissiphany.bany.adapter.json.DataEnvelopeFactory
import com.pissiphany.bany.adapter.json.LocalDateTimeAdapter
import com.squareup.moshi.Moshi
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.io.File

// See https://spin.atomicobject.com/2018/07/18/gradle-integration-tests/
class YnabServiceTest {
    companion object {
        private val YNAB_API_KEY_LOCATION = System.getProperty("user.dir") + "/../.ynab_api_key"

        private lateinit var service: YnabService

        @BeforeAll
        @JvmStatic
        internal fun setup() {
            val key = File(YNAB_API_KEY_LOCATION).readLines().first()
            val moshi = Moshi.Builder()
                .add(DataEnvelopeFactory())
                .add(LocalDateTimeAdapter())
                .build()
            val retrofit = RetrofitFactory.create("https://api.youneedabudget.com/", key, moshi)

            service = retrofit.create(YnabService::class.java)
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
}