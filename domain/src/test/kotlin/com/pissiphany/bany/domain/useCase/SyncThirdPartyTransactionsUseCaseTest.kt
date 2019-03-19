package com.pissiphany.bany.domain.useCase

import com.pissiphany.bany.domain.dataStructure.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

import java.time.OffsetDateTime

internal class SyncThirdPartyTransactionsUseCaseTest {
    @Test
    fun sync() {
        val budget = Budget("budgetId", "budgetName")
        val account = Account("accountId", "accountName", 1, false, Account.Type.CHECKING)
        val budgetAccounts = listOf(BudgetAccount(budget, account))

        val lastTransaction = Transaction("transactionId1", OffsetDateTime.now(), "payee", "memo", 2)

        val transactions = listOf(Transaction("transactionId2", OffsetDateTime.now(), "payee", "memo", 3))

        val results = listOf(SyncTransactionsResult(budget, account, lastTransaction.date, transactions))

        val step1 = Step1Test(budgetAccounts)
        val step2 = Step2Test(lastTransaction)
        val step3 = Step3Test(transactions)
        val step4 = Step4Test()
        val output = OutputBoundaryTest()

        val uc = SyncThirdPartyTransactionsUseCase(step1, step2, step3, step4, output)
        uc.sync()

        assertEquals(results, output.results)
    }

    private class Step1Test(private val budgetAccounts: List<BudgetAccount>) : SyncThirdPartyTransactionsUseCase.Step1GetBudgetAccounts {
        override fun getBudgetAccounts(): List<BudgetAccount> = budgetAccounts
    }

    private class Step2Test(private val transaction: Transaction) : SyncThirdPartyTransactionsUseCase.Step2GetMostRecentTransaction {
        override fun getTransaction(budget: Budget, account: Account): Transaction? = transaction
    }

    private class Step3Test(private val transactions: List<Transaction>) : SyncThirdPartyTransactionsUseCase.Step3GetNewTransactions {
        override fun getTransactions(budget: Budget, account: Account, date: LocalDate?): List<Transaction> = transactions
    }

    private class Step4Test: SyncThirdPartyTransactionsUseCase.Step4SaveNewTransactions {
        var transactions = emptyList<Transaction>()

        override fun saveTransactions(budget: Budget, account: Account, transactions: List<Transaction>) {
            this.transactions = transactions
        }
    }

    private class OutputBoundaryTest : SyncThirdPartyTransactionsOutputBoundary {
        var results: List<SyncTransactionsResult>? = null
        override fun present(results: List<SyncTransactionsResult>) { this.results = results }
    }
}