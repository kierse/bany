package com.pissiphany.bany.domain.useCase

import com.pissiphany.bany.domain.dataStructure.*
import com.pissiphany.bany.domain.repository.ConfigurationRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

import java.time.OffsetDateTime

@OptIn(ExperimentalCoroutinesApi::class)
internal class SyncThirdPartyTransactionsUseCaseTest {
    @Test
    fun sync() = runTest {
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

    @Test
    fun `sync - only zero dollar transactions`() = runTest {
        val budgetAccountIds = BudgetAccountIds(name = "name", budgetId = "budgetId", accountId = "accountId")
        val account = Account("accountId", "accountName", 1, false, Account.Type.CHECKING)

        val lastTransaction = AccountTransaction("transactionId1", OffsetDateTime.now(), "payee", "memo", 2)

        val transactions = listOf(AccountTransaction("transactionId2", OffsetDateTime.now(), "payee", "memo", 3))

        val repo = TestConfigRepo(listOf(budgetAccountIds))
        val step1 = Step1Test(AccountAndTransaction(account, lastTransaction))
        val step2 = Step2Test(transactions)
        val step3 = Step3Test(
            AccountTransaction(
                transactions[0].id,
                transactions[0].date,
                transactions[0].payee,
                transactions[0].memo,
                0
            )
        )
        val step4 = Step4Test()
        val output = OutputBoundaryTest()

        SyncThirdPartyTransactionsUseCase(repo, step1, step2, step3, step4, output).sync()

        assertEquals(emptyList<SyncTransactionsResult>(), output.results)
    }

    private class TestConfigRepo(private val budgetAccountIds: List<BudgetAccountIds>) : ConfigurationRepository {
        override suspend fun getBudgetAccountIds(): List<BudgetAccountIds> = budgetAccountIds
    }

    private class Step1Test(private val accountAndTransaction: AccountAndTransaction) : SyncThirdPartyTransactionsUseCase.Step1GetAccountDetails {
        override suspend fun getAccountAndLastTransaction(budgetAccountIds: BudgetAccountIds) = accountAndTransaction
    }

    private class Step2Test(private val transactions: List<Transaction>) : SyncThirdPartyTransactionsUseCase.Step2GetNewTransactions {
        override suspend fun getTransactions(budgetAccountIds: BudgetAccountIds, date: LocalDate?) = transactions
    }

    private class Step3Test(
        private val value: AccountTransaction? = null
    ): SyncThirdPartyTransactionsUseCase.Step3ProcessNewTransaction {
        override fun processTransaction(account: Account, newTransaction: Transaction): AccountTransaction {
            return value ?: newTransaction as AccountTransaction
        }
    }

    private class Step4Test: SyncThirdPartyTransactionsUseCase.Step4SaveNewTransactions {
        var transactions = emptyList<Transaction>()

        override suspend fun saveTransactions(budgetAccountIds: BudgetAccountIds, transactions: List<AccountTransaction>) {
            this.transactions = transactions
        }
    }

    private class OutputBoundaryTest : SyncThirdPartyTransactionsOutputBoundary {
        var results: List<SyncTransactionsResult>? = null
        override fun present(results: List<SyncTransactionsResult>) { this.results = results }
    }
}