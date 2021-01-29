package com.pissiphany.bany.service

import com.pissiphany.bany.adapter.dataStructure.*
import com.pissiphany.bany.dataStructure.*
import com.pissiphany.bany.mapper.RetrofitAccountMapper
import com.pissiphany.bany.mapper.RetrofitTransactionMapper
import okhttp3.Request
import okio.Timeout
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

internal class RetrofitYnabApiServiceTest {
    @Test
    fun getAccount() {
        val ynabService = TestService(
            budgetId = "budgetId", accountId = "accountId", account = RetrofitAccount("accountId", "name", false, 5, "checking")
        )
        val service = RetrofitYnabApiService(ynabService, RetrofitAccountMapper(), RetrofitTransactionMapper())

        assertEquals(
            YnabAccount("accountId", "name", false, 5, "checking"),
            service.getAccount(YnabBudgetAccountIds("budgetId", "accountId"))
        )
    }

    @Test
    fun getTransactions() {
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

        val ynabService = TestService(
            budgetId = "budgetId",
            accountId = "accountId",
            lastKnowledgeOfServer = 10,
            transactionsWrapper = transactionsWrapper
        )
        val service = RetrofitYnabApiService(ynabService, RetrofitAccountMapper(), RetrofitTransactionMapper())

        assertEquals(
            expected,
            service.getTransactions(budgetAccountIds, 10)
        )
    }

    @Test
    fun saveTransactions() {
        val now = OffsetDateTime.now()
        val ids = YnabBudgetAccountIds(ynabBudgetId = "budgetId", ynabAccountId = "accountId")
        val ynabTransactions = listOf(
            YnabAccountTransaction("transactionId", "accountId", now, "payee", "memo", 10)
        )

        val ynabService = TestService(budgetId = "budgetId")
        val service = RetrofitYnabApiService(
            ynabService, RetrofitAccountMapper(), RetrofitTransactionMapper()
        )

        assertTrue(service.saveTransactions(ids, ynabTransactions))
    }

    private class TestService(
        private val budgetId: String,
        private val accountId: String = "",
        private val lastKnowledgeOfServer: Int? = null,
        private val budgetWrapper: RetrofitBudgetWrapper? = null,
        private val account: RetrofitAccount? = null,
        private val transactionsWrapper: RetrofitTransactionsWrapper? = null
    ) : RetrofitYnabService {
        override fun getTransactions(
            budgetId: String, accountId: String, lastKnowledgeOfServer: Int?
        ): Call<RetrofitTransactionsWrapper> {
            val assertion = this.budgetId == budgetId
                    && this.accountId == accountId
                    && this.lastKnowledgeOfServer == lastKnowledgeOfServer
            return TestCall(if (assertion) transactionsWrapper else null)
        }

        override fun saveTransactions(budgetId: String, transactions: RetrofitTransactions): Call<Unit> {
            return TestCall(if (this.budgetId == budgetId) Unit else null)
        }

        override fun getBudget(budgetId: String): Call<RetrofitBudgetWrapper> {
            return TestCall(if (this.budgetId == budgetId) budgetWrapper else null)
        }

        override fun getAccount(budgetId: String, accountId: String): Call<RetrofitAccount> {
            return TestCall(if (budgetId == this.budgetId && accountId == this.accountId) account else null)
        }

        /** NOT NEEDED **/
        override fun getBudgets(): Call<RetrofitBudgets> { TODO("not implemented") }
        override fun getAccounts(budgetId: String): Call<RetrofitAccounts> { TODO("not implemented") }
    }

    private class TestCall<T>(private val obj: T?) : Call<T> {
        override fun execute(): Response<T> = Response.success(obj)

        /** NOT NEEDED **/
        override fun enqueue(callback: Callback<T>) { TODO("not implemented") }
        override fun isExecuted(): Boolean { TODO("not implemented") }
        override fun clone(): Call<T> { TODO("not implemented") }
        override fun isCanceled(): Boolean { TODO("not implemented") }
        override fun cancel() { TODO("not implemented") }
        override fun request(): Request { TODO("not implemented") }
        override fun timeout(): Timeout { TODO("Not yet implemented") }
    }
}