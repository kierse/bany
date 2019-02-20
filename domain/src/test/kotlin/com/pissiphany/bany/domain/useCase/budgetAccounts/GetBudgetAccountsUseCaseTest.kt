package com.pissiphany.bany.domain.useCase.budgetAccounts

import com.pissiphany.bany.domain.dataStructure.Account
import com.pissiphany.bany.domain.dataStructure.Budget
import com.pissiphany.bany.domain.dataStructure.BudgetAccount
import com.pissiphany.bany.domain.dataStructure.BudgetAccountIds
import com.pissiphany.bany.domain.repository.ConfigurationRepository
import com.pissiphany.bany.domain.gateway.YnabBudgetAccountsGateway
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.IllegalArgumentException

internal class GetBudgetAccountsUseCaseTest {
    companion object {
        const val budgetId = "budgetId"
        const val accountId = "accountId"
    }

    private val budget = Budget(id = "id1", name = "name1")
    private val account = Account(id = "id", name = "name", balance = 1L, closed = false, type = Account.Type.CHECKING)

    @Test
    fun run__success() {
        val boundary = TestBoundary()
        val gateway = TestGateway(budget, account)
        val repo = TestRepo(listOf(
            BudgetAccountIds(budgetId = budgetId, accountId = accountId)
        ))

        val uc = GetBudgetAccountsUseCase(repo, gateway)

        uc.run(boundary)

        assertIterableEquals(listOf(BudgetAccount(budget, account)), boundary.budgetAccounts)
    }

    @Test
    fun run__cache_hit() {
        val gateway = TestGateway(budget, account)

        // note: requesting the same budget / account twice to trigger a cache lookup
        val repo = TestRepo(listOf(
            BudgetAccountIds(budgetId = budgetId, accountId = accountId),
            BudgetAccountIds(budgetId = budgetId, accountId = accountId)
        ))

        val uc = GetBudgetAccountsUseCase(repo, gateway)

        uc.run(TestBoundary()) // makes service call

        assertEquals(gateway.getBudgetCallCount, 1)
    }

    @Test
    fun run__budget_not_found() {
        val gateway = TestGateway(budget, account)
        val repo = TestRepo(listOf(
            BudgetAccountIds(budgetId = "foo", accountId = accountId)
        ))

        assertThrows<IllegalArgumentException> {
            GetBudgetAccountsUseCase(repo, gateway).run(TestBoundary())
        }
    }

    @Test
    fun run__account_not_found() {
        val gateway = TestGateway(budget, account)
        val repo = TestRepo(listOf(
            BudgetAccountIds(budgetId = budgetId, accountId = "bar")
        ))

        assertThrows<IllegalArgumentException> {
            GetBudgetAccountsUseCase(repo, gateway).run(TestBoundary())
        }
    }

    private class TestGateway(private val budget: Budget, private val account: Account) : YnabBudgetAccountsGateway {
        private val budgetId = GetBudgetAccountsUseCaseTest.budgetId
        private val accountId = GetBudgetAccountsUseCaseTest.accountId
        var getBudgetCallCount = 0
            private set

        override fun getBudget(budgetId: String): Budget? {
            getBudgetCallCount++
            if (this.budgetId == budgetId) return budget
            return null
        }

        override fun getAccount(budgetId: String, accountId: String): Account? {
            if (this.accountId == accountId) return account
            return null
        }
    }

    private class TestRepo(private val budgetAccountIds: List<BudgetAccountIds>) : ConfigurationRepository {
        override fun getBudgetAccountIds(): List<BudgetAccountIds> = budgetAccountIds
    }

    private class TestBoundary : GetBudgetAccountsOutputBoundary {
        override var budgetAccounts: List<BudgetAccount> = emptyList()
    }
}