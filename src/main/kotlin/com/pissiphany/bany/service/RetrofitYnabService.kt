package com.pissiphany.bany.service

import com.pissiphany.bany.annotation.DataEnvelope
import com.pissiphany.bany.dataStructure.*
import retrofit2.Response
import retrofit2.http.*

interface RetrofitYnabService {
    @DataEnvelope
    @GET("/v1/budgets")
    suspend fun getBudgets(): Response<RetrofitBudgets>

    @DataEnvelope
    @GET("/v1/budgets/{budget_id}")
    suspend fun getBudget(@Path("budget_id") budgetId: String): Response<RetrofitBudgetWrapper>

    @DataEnvelope
    @GET("/v1/budgets/{budget_id}/accounts")
    suspend fun getAccounts(@Path("budget_id") budgetId: String): Response<RetrofitAccounts>

    @DataEnvelope(wrappers = 2)
    @GET("/v1/budgets/{budget_id}/accounts/{account_id}")
    suspend fun getAccount(
        @Path("budget_id") budgetId: String,
        @Path("account_id") accountId: String
    ): Response<RetrofitAccount>

    @DataEnvelope
    @GET("/v1/budgets/{budget_id}/accounts/{account_id}/transactions")
    suspend fun getTransactions(
        @Path("budget_id") budgetId: String,
        @Path("account_id") accountId: String,
        @Query("last_knowledge_of_server") lastKnowledgeOfServer: Int? = null
    ): Response<RetrofitTransactionsWrapper>

    @POST("/v1/budgets/{budget_id}/transactions")
    suspend fun saveTransactions(
        @Path("budget_id") budgetId: String,
        @Body transactions: RetrofitTransactions
    ): Response<Unit>
}