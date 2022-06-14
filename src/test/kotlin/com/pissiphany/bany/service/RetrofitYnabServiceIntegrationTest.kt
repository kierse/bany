package com.pissiphany.bany.service

import com.pissiphany.bany.BASE_URL
import com.pissiphany.bany.adapter.LocalDateAdapter
import com.pissiphany.bany.adapter.OffsetDateTimeAdapter
import com.pissiphany.bany.dataStructure.RetrofitTransaction
import com.pissiphany.bany.dataStructure.RetrofitTransactions
import com.pissiphany.bany.factory.DataEnvelopeFactory
import com.pissiphany.bany.factory.RetrofitFactory
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.logging.HttpLoggingInterceptor
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.io.File
import java.time.LocalDate

private val CONFIG_DIR = File(System.getProperty("user.home"), ".bany")
private val CONFIG_FILE = File(CONFIG_DIR, "ynab-integration.config")

// See https://spin.atomicobject.com/2018/07/18/gradle-integration-tests/
@OptIn(ExperimentalCoroutinesApi::class)
class RetrofitYnabServiceIntegrationTest {
    companion object {
        private lateinit var config: YnabConfig
        private lateinit var service: RetrofitYnabService

        @BeforeAll
        @JvmStatic
        internal fun setup() {
            val moshi = Moshi.Builder()
                .add(DataEnvelopeFactory())
                .add(LocalDateAdapter())
                .add(OffsetDateTimeAdapter())
                .build()

            config = CONFIG_FILE
                .takeIf(File::isFile)
                ?.let { file ->
                    val adapter = moshi.adapter(YnabConfig::class.java)
                    adapter.fromJson(file.readText())
                }
                ?: throw UnknownError("unable to read config file! Does file exist at $CONFIG_FILE")

            service = RetrofitFactory.create(BASE_URL, config.apiToken, moshi, HttpLoggingInterceptor.Level.BODY)
                .run {
                    create(RetrofitYnabService::class.java)
                }
        }
    }

    @Test
    fun getBudgets() = runTest {
        val response = service.getBudgets()
        val body = response.body()

        assertTrue(response.isSuccessful)
        assertTrue(body?.budgets?.isNotEmpty() ?: false)
    }

    @Test
    fun getBudget() = runTest {
        val response = service.getBudget(config.budgetId)
        val body = response.body()

        assertTrue(response.isSuccessful)
        assertEquals(config.budgetId, body?.budget?.id)
    }

    @Test
    fun getAccounts() = runTest {
        val budgetsResponse = service.getBudgets()
        val budget = budgetsResponse.body()?.budgets?.first() ?: fail("Unable to retrieve budgets")

        val accountsResponse = service.getAccounts(budget.id)
        val body = accountsResponse.body()

        assertTrue(accountsResponse.isSuccessful)
        assertTrue(body?.accounts?.isNotEmpty() ?: false)
    }

    @Test
    fun `getAccounts - unknown budget`() = runTest {
        val accountsResponse = service.getAccounts("fake-budget-id")

        assertFalse(accountsResponse.isSuccessful)
        assertEquals(404, accountsResponse.code())
    }

    @Test
    fun getAccount() = runTest {
        val response = service.getAccount(config.budgetId, config.accountId)
        val account = response.body()

        assertTrue(response.isSuccessful)
        assertEquals(config.accountId, account?.id)
    }

    @Test
    fun saveTransactions() = runTest {
        val data = RetrofitTransactions(
            transactions = listOf(
                RetrofitTransaction(
                    account_id = config.accountId,
                    date = LocalDate.now().minusDays(2L),
                    payee_name = "payee1",
                    memo = "memo1",
                    amount = 10
                ),
                RetrofitTransaction(
                    account_id = config.accountId,
                    date = LocalDate.now().minusDays(1L),
                    payee_name = "payee2",
                    memo = "memo2",
                    amount = 20
                )
            )
        )

        val response = service.saveTransactions(config.budgetId, data)

        assertTrue(response.isSuccessful)
    }

    @JsonClass(generateAdapter = true)
    data class YnabConfig(
        val apiToken: String,
        val budgetId: String,
        val accountId: String
    )
}