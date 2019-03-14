package com.pissiphany.bany.service

import com.pissiphany.bany.annotation.DataEnvelope
import com.pissiphany.bany.dataStructure.*
import retrofit2.Call
import retrofit2.http.*

interface RetrofitYnabService {
    @DataEnvelope
    @GET("/v1/budgets")
    fun getBudgets(): Call<RetrofitBudgets>

    @DataEnvelope
    @GET("/v1/budgets/{budget_id}")
    fun getBudget(@Path("budget_id") budgetId: String): Call<RetrofitBudgetWrapper>

    @DataEnvelope
    @GET("/v1/budgets/{budget_id}/accounts")
    fun getAccounts(@Path("budget_id") budgetId: String): Call<RetrofitAccounts>

    @DataEnvelope(wrappers = 2)
    @GET("/v1/budgets/{budget_id}/accounts/{account_id}")
    fun getAccount(@Path("budget_id") budgetId: String, @Path("account_id") accountId: String): Call<RetrofitAccount>

    @DataEnvelope
    @GET("/v1/budgets/{budget_id}/accounts/{account_id}/transactions")
    fun getTransactions(
        @Path("budget_id") budgetId: String,
        @Path("account_id") accountId: String,
        @Query("last_knowledge_of_server") lastKnowledgeOfServer: Int? = null
    ): Call<RetrofitTransactionsWrapper>

    @POST("/v1/budgets/{budget_id}/transactions")
    fun saveTransactions(@Path("budget_id") budgetId: String, @Body transactions: RetrofitTransactions): Call<Unit>
}