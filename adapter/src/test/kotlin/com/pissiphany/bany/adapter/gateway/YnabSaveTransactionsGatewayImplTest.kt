package com.pissiphany.bany.adapter.gateway

import com.pissiphany.bany.adapter.dataStructure.*
import com.pissiphany.bany.adapter.mapper.TransactionMapper
import com.pissiphany.bany.adapter.service.YnabService
import com.pissiphany.bany.domain.dataStructure.Account
import com.pissiphany.bany.domain.dataStructure.Budget
import com.pissiphany.bany.domain.dataStructure.Transaction
import okhttp3.Request
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalTime

internal class YnabSaveTransactionsGatewayImplTest {
    @Test
    fun saveTransactions() {
        val budget = Budget("budgetId", "budgetName")
        val account = Account("accountId", "accountName", 3L, false, Account.Type.CHECKING)
        val date = LocalTime.now()
        val transactions = listOf(
            Transaction("transactionId", date, 10L)
        )
        val ynabTransactions = YnabTransactions(listOf(
            YnabTransaction("transactionId", "accountId", 10L, date)
        ))

        val service = TestService()
        val gateway = YnabSaveTransactionsGatewayImpl(service, TransactionMapper())

        assertTrue(gateway.saveTransactions(budget, account, transactions))
        assertEquals("budgetId", service.budgetId)
        assertEquals(ynabTransactions, service.ynabTransactions)
    }

    private class TestService : YnabService {
        var budgetId: String? = null
            private set
        var ynabTransactions: YnabTransactions? = null
            private set

        override fun saveTransactions(budgetId: String, transactions: YnabTransactions): Call<Unit> {
            this.budgetId = budgetId
            this.ynabTransactions = transactions

            return TestCall()
        }

        override fun getBudgets(): Call<YnabBudgets> { TODO("not implemented") }
        override fun getBudget(budgetId: String): Call<YnabBudgetWrapper> { TODO("not implemented") }
        override fun getAccounts(budgetId: String): Call<YnabAccounts> { TODO("not implemented") }
        override fun getAccount(budgetId: String, accountId: String): Call<YnabAccount> { TODO("not implemented") }
    }

    private class TestCall : Call<Unit> {
        override fun execute(): Response<Unit> {
            return Response.success(Unit)
        }

        override fun enqueue(callback: Callback<Unit>) { TODO("not implemented") }
        override fun isExecuted(): Boolean { TODO("not implemented") }
        override fun clone(): Call<Unit> { TODO("not implemented") }
        override fun isCanceled(): Boolean { TODO("not implemented") }
        override fun cancel() { TODO("not implemented") }
        override fun request(): Request { TODO("not implemented") }
    }
}