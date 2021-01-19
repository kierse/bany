package com.pissiphany.bany.domain.useCase

import com.pissiphany.bany.domain.dataStructure.*
import com.pissiphany.bany.domain.repository.ConfigurationRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

import java.time.OffsetDateTime

internal class SyncThirdPartyTransactionsUseCaseTest {
    @Test
    fun sync() {
        val budgetAccountIds = BudgetAccountIds(name = "name", budgetId = "budgetId", accountId = "accountId")
        val account = Account("accountId", "accountName", 1, false, Account.Type.CHECKING)

        val lastTransaction = AccountTransaction("transactionId1", OffsetDateTime.now(), "payee", "memo", 2)

        val transactions = listOf(AccountTransaction("transactionId2", OffsetDateTime.now(), "payee", "memo", 3))

        val results = listOf(SyncTransactionsResult(budgetAccountIds, lastTransaction.date, transactions))

        val repo = TestConfigRepo(listOf(budgetAccountIds))
        val step1 = Step1Test(AccountAndTransaction(account, lastTransaction))
        val step2 = Step2Test(transactions)
        val step3 = Step3Test()
        val step4 = Step4Test()
        val output = OutputBoundaryTest()

        SyncThirdPartyTransactionsUseCase(repo, step1, step2, step3, step4, output).sync()

        assertEquals(results, output.results)
    }

    private class TestConfigRepo(private val budgetAccountIds: List<BudgetAccountIds>) : ConfigurationRepository {
        override fun getBudgetAccountIds(): List<BudgetAccountIds> = budgetAccountIds
    }

    private class Step1Test(private val accountAndTransaction: AccountAndTransaction) : SyncThirdPartyTransactionsUseCase.Step1GetAccountDetails {
        override fun getAccountAndLastTransaction(budgetAccountIds: BudgetAccountIds) = accountAndTransaction
    }

    private class Step2Test(private val transactions: List<Transaction>) : SyncThirdPartyTransactionsUseCase.Step2GetNewTransactions {
        override fun getTransactions(budgetAccountIds: BudgetAccountIds, date: LocalDate?) = transactions
    }

    private class Step3Test: SyncThirdPartyTransactionsUseCase.Step3ProcessNewTransaction {
        override fun processTransaction(account: Account, newTransaction: Transaction) = newTransaction as AccountTransaction
    }

    private class Step4Test: SyncThirdPartyTransactionsUseCase.Step4SaveNewTransactions {
        var transactions = emptyList<Transaction>()

        override fun saveTransactions(budgetAccountIds: BudgetAccountIds, transactions: List<AccountTransaction>) {
            this.transactions = transactions
        }
    }

    private class OutputBoundaryTest : SyncThirdPartyTransactionsOutputBoundary {
        var results: List<SyncTransactionsResult>? = null
        override fun present(results: List<SyncTransactionsResult>) { this.results = results }
    }
}