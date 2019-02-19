package com.pissiphany.bany.adapter.gateway

import com.pissiphany.bany.adapter.dataStructure.*
import com.pissiphany.bany.adapter.mapper.AccountMapper
import com.pissiphany.bany.adapter.mapper.BudgetMapper
import com.pissiphany.bany.adapter.service.YnabService
import com.pissiphany.bany.domain.dataStructure.Account
import com.pissiphany.bany.domain.dataStructure.Budget
import okhttp3.Request
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime

internal class YnabBudgetAccountsGatewayImplTest {
    @Test
    fun getBudget() {
        val service = TestService(key = "budgetId", budget = YnabBudget("budgetId", "name", LocalDateTime.now()))
        val gateway = YnabBudgetAccountsGatewayImpl(service, BudgetMapper(), AccountMapper())

        assertEquals(Budget("budgetId", "name"), gateway.getBudget("budgetId"))
    }

    @Test
    fun getAccount() {
        val service = TestService(key = "accountId", account = YnabAccount("accountId", "name", false, 5L, "checking"))
        val gateway = YnabBudgetAccountsGatewayImpl(service, BudgetMapper(), AccountMapper())

        assertEquals(Account("accountId", "name", 5L, false, Account.Type.CHECKING), gateway.getAccount("accountId"))
    }

    private class TestService(
        private val key: String, private val budget: YnabBudget? = null, private val account: YnabAccount? = null
    ) : YnabService {
        override fun getBudget(budgetId: String): Call<YnabBudget> {
            return TestCall(if (budgetId == key) budget else null)
        }

        override fun getAccount(accountId: String): Call<YnabAccount> {
            return TestCall(if (accountId == key) account else null)
        }

        /** NOT NEEDED **/
        override fun getBudgets(): Call<YnabBudgets> { TODO("not implemented") }
        override fun getAccounts(budgetId: String): Call<YnabAccounts> { TODO("not implemented") }
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

    }
}