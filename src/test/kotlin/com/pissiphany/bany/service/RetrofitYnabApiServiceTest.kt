package com.pissiphany.bany.service

import com.pissiphany.bany.adapter.dataStructure.*
import com.pissiphany.bany.dataStructure.*
import com.pissiphany.bany.mapper.RetrofitAccountMapper
import com.pissiphany.bany.mapper.RetrofitTransactionMapper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.IOException
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import retrofit2.Response
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
class RetrofitYnabApiServiceTest {
    @Test
    fun getAccount() = runTest {
        val account = RetrofitAccount("accountId", "name", false, 5, "checking")
        val ynabService = TestService { Response.success(account) }
        val service = RetrofitYnabApiService(ynabService, RetrofitAccountMapper(), RetrofitTransactionMapper())

        assertEquals(
            YnabAccount("accountId", "name", false, 5, "checking"),
            service.getAccount(YnabBudgetAccountIds("budgetId", "accountId"))
        )
    }

    @Test
    fun `getAccount - null response body`() = runTest {
        val ynabService = TestService { Response.success(null) }
        val service = RetrofitYnabApiService(ynabService, RetrofitAccountMapper(), RetrofitTransactionMapper())

        assertNull(service.getAccount(YnabBudgetAccountIds("budgetId", "accountId")))
    }

    @Test
    fun `getAccount - call fails with exception`() = runTest {
        val ynabService = TestService { throw IOException("foo!") }
        val service = RetrofitYnabApiService(ynabService, RetrofitAccountMapper(), RetrofitTransactionMapper())

        assertNull(service.getAccount(YnabBudgetAccountIds("budgetId", "accountId")))
    }

    @Test
    fun getTransactions() = runTest {
        val now = LocalDate.now()
        val budgetAccountIds = YnabBudgetAccountIds("budgetId", "accountId")
        val transactionsWrapper = RetrofitTransactionsWrapper(
            listOf(RetrofitTransaction("transactionId", "accountId", now, "payee", "memo", 7)), 10
        )

        val expectedTime = OffsetDateTime.of(now, LocalTime.MIN, ZoneOffset.UTC)
        val expected = YnabUpdatedTransactions(
            listOf(YnabAccountTransaction("transactionId", "accountId", expectedTime, "payee", "memo", 7)),
            10
        )

        val ynabService = TestService { Response.success(transactionsWrapper) }
        val service = RetrofitYnabApiService(ynabService, RetrofitAccountMapper(), RetrofitTransactionMapper())

        assertEquals(
            expected,
            service.getTransactions(budgetAccountIds, 10)
        )
    }

    @Test
    fun `getTransactions - call fails with exception`() = runTest {
        val budgetAccountIds = YnabBudgetAccountIds("budgetId", "accountId")

        val ynabService = TestService { throw IOException("foo!") }
        val service = RetrofitYnabApiService(ynabService, RetrofitAccountMapper(), RetrofitTransactionMapper())

        assertThrows<IOException> { service.getTransactions(budgetAccountIds, 10) }
    }

    @Test
    fun `getTransactions - null response body fails with exception`() = runTest {
        val budgetAccountIds = YnabBudgetAccountIds("budgetId", "accountId")

        val ynabService = TestService { Response.success(null) }
        val service = RetrofitYnabApiService(ynabService, RetrofitAccountMapper(), RetrofitTransactionMapper())

        assertThrows<IllegalStateException> { service.getTransactions(budgetAccountIds, 10) }
    }

    @Test
    fun saveTransactions() = runTest {
        val now = OffsetDateTime.now()
        val ids = YnabBudgetAccountIds(ynabBudgetId = "budgetId", ynabAccountId = "accountId")
        val ynabTransactions = listOf(
            YnabAccountTransaction("transactionId", "accountId", now, "payee", "memo", 10)
        )

        val ynabService = TestService { Response.success(Unit) }
        val service = RetrofitYnabApiService(
            ynabService, RetrofitAccountMapper(), RetrofitTransactionMapper()
        )

        assertTrue(service.saveTransactions(ids, ynabTransactions))
    }

    @Test
    fun `saveTransactions - null response body`() = runTest {
        val now = OffsetDateTime.now()
        val ids = YnabBudgetAccountIds(ynabBudgetId = "budgetId", ynabAccountId = "accountId")
        val ynabTransactions = listOf(
            YnabAccountTransaction("transactionId", "accountId", now, "payee", "memo", 10)
        )

        val ynabService = TestService { Response.error<Unit>(500, byteArrayOf().toResponseBody()) }
        val service = RetrofitYnabApiService(
            ynabService, RetrofitAccountMapper(), RetrofitTransactionMapper()
        )

        assertFalse(service.saveTransactions(ids, ynabTransactions))
    }

    @Test
    fun `saveTransactions - call fails with exception`() = runTest {
        val ids = YnabBudgetAccountIds(ynabBudgetId = "budgetId", ynabAccountId = "accountId")

        val ynabService = TestService { throw IOException("foo!") }
        val service = RetrofitYnabApiService(
            ynabService, RetrofitAccountMapper(), RetrofitTransactionMapper()
        )

        assertFalse(service.saveTransactions(ids, emptyList()))
    }

    @Suppress("UNCHECKED_CAST")
    private class TestService(private val response: () -> Response<*>) : RetrofitYnabService {
        override suspend fun getAccount(budgetId: String, accountId: String): Response<RetrofitAccount> {
            return response() as Response<RetrofitAccount>
        }

        override suspend fun getTransactions(
            budgetId: String,
            accountId: String,
            lastKnowledgeOfServer: Int?
        ): Response<RetrofitTransactionsWrapper> {
            return response() as Response<RetrofitTransactionsWrapper>
        }

        override suspend fun saveTransactions(budgetId: String, transactions: RetrofitTransactions): Response<Unit> {
            return response() as Response<Unit>
        }

        /** NOT NEEDED **/
        override suspend fun getBudgets(): Response<RetrofitBudgets> { TODO("not implemented") }
        override suspend fun getBudget(budgetId: String): Response<RetrofitBudgetWrapper> { TODO("not implemented") }
        override suspend fun getAccounts(budgetId: String): Response<RetrofitAccounts> { TODO("not implemented") }
    }
}