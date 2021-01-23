package com.pissiphany.bany.service

import com.pissiphany.bany.BASE_URL
import com.pissiphany.bany.adapter.OffsetDateTimeAdapter
import com.pissiphany.bany.factory.DataEnvelopeFactory
import com.pissiphany.bany.factory.RetrofitFactory
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.io.File

private val CONFIG_DIR = File(System.getProperty("user.home"), ".bany")
private val CONFIG_FILE = File(CONFIG_DIR, "ynab-integration.config")

// See https://spin.atomicobject.com/2018/07/18/gradle-integration-tests/
class RetrofitYnabServiceIntegrationTest {
    companion object {
        private lateinit var config: YnabConfig
        private lateinit var service: RetrofitYnabService

        @BeforeAll
        @JvmStatic
        internal fun setup() {
            val moshi = Moshi.Builder()
                .add(DataEnvelopeFactory())
                .add(OffsetDateTimeAdapter())
                .build()

            config = CONFIG_FILE
                .takeIf(File::isFile)
                ?.let { file ->
                    val adapter = moshi.adapter(YnabConfig::class.java)
                    adapter.fromJson(file.readText())
                }
                ?: throw UnknownError("unable to read config file!")

            service = RetrofitFactory.create(BASE_URL, config.apiToken, moshi)
                .run {
                    create(RetrofitYnabService::class.java)
                }
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
    fun getBudget() {
        val call = service.getBudget(config.budgetId)
        val response = call.execute()
        val body = response.body()

        assertTrue(response.isSuccessful)
        assertEquals(config.budgetId, body?.budget?.id)
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

    @Test
    fun getAccount() {
        val call = service.getAccount(config.budgetId, config.accountId)
        val response = call.execute()
        val account = response.body()

        assertTrue(response.isSuccessful)
        assertEquals(config.accountId, account?.id)
    }

    @JsonClass(generateAdapter = true)
    data class YnabConfig(
        val apiToken: String,
        val budgetId: String,
        val accountId: String
    )
}