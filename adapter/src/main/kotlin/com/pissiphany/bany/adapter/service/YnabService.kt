package com.pissiphany.bany.adapter.service

import com.pissiphany.bany.adapter.annotation.DataEnvelope
import com.pissiphany.bany.adapter.dataStructure.YnabAccounts
import com.pissiphany.bany.adapter.dataStructure.YnabBudgets
import com.pissiphany.bany.domain.dataStructure.Transaction
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.time.LocalDate

interface YnabService {
    @DataEnvelope
    @GET("/v1/budgets")
    fun getBudgets(): Call<YnabBudgets>

    @DataEnvelope
    @GET("/v1/budgets/{budget_id}/accounts")
    fun getAccounts(@Path("budget_id") budgetId: String): Call<YnabAccounts>

    @GET("/v1/budgets/{budgetId}/accounts/{accountId}/transactions")
    fun getTransactionsSince(budgetId: String, accountId: String, @Query("since_date") since: LocalDate): Call<List<Transaction>>

    @GET("/v1/budgets/{budgetId}/accounts/{accountId}/transactions")
    fun getTransaction(budgetId: String, accountId: String): Call<List<Transaction>>
}