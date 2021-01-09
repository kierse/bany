package com.pissiphany.bany.service

import com.pissiphany.bany.adapter.dataStructure.*
import com.pissiphany.bany.dataStructure.*
import com.pissiphany.bany.mapper.RetrofitAccountMapper
import com.pissiphany.bany.mapper.RetrofitBudgetMapper
import com.pissiphany.bany.mapper.RetrofitTransactionMapper
import okhttp3.Request
import okio.Timeout
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.OffsetDateTime

internal class RetrofitYnabApiServiceTest {
    @Test
    fun getBudget() {
        val now = OffsetDateTime.now()
        val budgetWrapper = RetrofitBudgetWrapper(RetrofitBudget("budgetId", "name", now), 10)
        val ynabService = TestService(key1 = "budgetId", budgetWrapper = budgetWrapper)
        val service = RetrofitYnabApiService(
            ynabService, RetrofitBudgetMapper(), RetrofitAccountMapper(), RetrofitTransactionMapper()
        )

        assertEquals(YnabBudget("budgetId", "name", now), service.getBudget("budgetId"))
    }

    @Test
    fun getAccount() {
        val ynabService = TestService(
            key1 = "budgetId", key2 = "accountId", account = RetrofitAccount("accountId", "name", false, 5, "checking")
        )
        val service = RetrofitYnabApiService(
            ynabService, RetrofitBudgetMapper(), RetrofitAccountMapper(), RetrofitTransactionMapper()
        )

        assertEquals(YnabAccount("accountId", "name", false, 5, "checking"), service.getAccount("budgetId", "accountId"))
    }

    @Test
    fun getTransactions() {
        val now = OffsetDateTime.now()
        val budget = YnabBudget("budgetId", "name", now)
        val account = YnabAccount("accountId", "name", false, 5, "checking")
        val transactionsWrapper = RetrofitTransactionsWrapper(
            listOf(RetrofitTransaction("transactionId", "accountId", now, "payee", "memo", 7)), 10
        )
        val ynabService = TestService(
            key1 = budget.id, key2 = account.id, key3 = 10, transactionsWrapper = transactionsWrapper
        )
        val service = RetrofitYnabApiService(
            ynabService, RetrofitBudgetMapper(), RetrofitAccountMapper(), RetrofitTransactionMapper()
        )

        assertEquals(
            YnabUpdatedTransactions(listOf(YnabTransaction("transactionId", account.id, now, "payee", "memo", 7)), 10),
            service.getTransactions(budget, account, 10)
        )
    }

    @Test
    fun saveTransactions() {
        val now = OffsetDateTime.now()
        val budget = YnabBudget("budgetId", "name", now)
        val ynabTransactions = listOf(
            YnabTransaction("transactionId", "accountId", now, "payee", "memo", 10)
        )

        val ynabService = TestService(key1 = "budgetId")
        val service = RetrofitYnabApiService(
            ynabService, RetrofitBudgetMapper(), RetrofitAccountMapper(), RetrofitTransactionMapper()
        )

        assertTrue(service.saveTransactions(budget, ynabTransactions))
    }

    private class TestService(
        private val key1: String,
        private val key2: String = "",
        private val key3: Int? = null,
        private val budgetWrapper: RetrofitBudgetWrapper? = null,
        private val account: RetrofitAccount? = null,
        private val transactionsWrapper: RetrofitTransactionsWrapper? = null
    ) : RetrofitYnabService {
        override fun getTransactions(
            budgetId: String, accountId: String, lastKnowledgeOfServer: Int?
        ): Call<RetrofitTransactionsWrapper> {
            return TestCall(
                if (key1 == budgetId && key2 == accountId && key3 == lastKnowledgeOfServer) transactionsWrapper else null
            )
        }

        override fun saveTransactions(budgetId: String, transactions: RetrofitTransactions): Call<Unit> {
            return TestCall(if (key1 == budgetId) Unit else null)
        }

        override fun getBudget(budgetId: String): Call<RetrofitBudgetWrapper> {
            return TestCall(if (key1 == budgetId) budgetWrapper else null)
        }

        override fun getAccount(budgetId: String, accountId: String): Call<RetrofitAccount> {
            return TestCall(if (budgetId == key1 && accountId == key2) account else null)
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