package com.pissiphany.bany.adapter.service

import com.pissiphany.bany.adapter.annotation.DataEnvelope
import com.pissiphany.bany.adapter.dataStructure.*
import retrofit2.Call
import retrofit2.http.*

interface YnabService {
    @DataEnvelope
    @GET("/v1/budgets")
    fun getBudgets(): Call<YnabBudgets>

    @DataEnvelope
    @GET("/v1/budgets/{budget_id}")
    fun getBudget(@Path("budget_id") budgetId: String): Call<YnabBudgetWrapper>

    @DataEnvelope
    @GET("/v1/budgets/{budget_id}/accounts")
    fun getAccounts(@Path("budget_id") budgetId: String): Call<YnabAccounts>

    @DataEnvelope(wrappers = 2)
    @GET("/v1/budgets/{budget_id}/accounts/{account_id}")
    fun getAccount(@Path("budget_id") budgetId: String, @Path("account_id") accountId: String): Call<YnabAccount>

    @DataEnvelope
    @GET("/v1/budgets/{budget_id}/accounts/{account_id}/transactions")
    fun getTransactions(
        @Path("budget_id") budgetId: String,
        @Path("account_id") accountId: String,
        @Query("last_knowledge_of_server") serverKnowledge: Int? = null
    ): Call<YnabTransactionsWrapper>

    @POST("/v1/budgets/{budget_id}/transactions")
    fun saveTransactions(@Path("budget_id") budgetId: String, @Body transactions: YnabTransactions): Call<Unit>
}