package com.pissiphany.bany.domain.useCase.step

import com.pissiphany.bany.domain.dataStructure.Account
import com.pissiphany.bany.domain.dataStructure.Budget
import com.pissiphany.bany.domain.dataStructure.BudgetAccount
import com.pissiphany.bany.domain.dataStructure.BudgetAccountIds
import com.pissiphany.bany.domain.gateway.YnabBudgetAccountsGateway
import com.pissiphany.bany.domain.repository.ConfigurationRepository
import org.junit.jupiter.api.Test
import java.lang.IllegalArgumentException

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows

internal class GetBudgetAccountsTest {
    private companion object {
        const val budgetId = "budgetId"
        const val accountId = "accountId"
    }

    private val budget = Budget(id = "id1", name = "name1")
    private val account = Account(id = "id", name = "name", balanceInCents = 1, closed = false, type = Account.Type.CHECKING)

    @Test
    fun getBudgetAccounts__success() {
        val gateway = TestGateway(budget, account)
        val repo = TestRepo(listOf(
            BudgetAccountIds(budgetId = budgetId, accountId = accountId)
        ))

        val step = GetBudgetAccounts(repo, gateway)

        assertIterableEquals(listOf(BudgetAccount(budget, account)), step.getBudgetAccounts())
    }

    @Test
    fun getBudgetAccounts__cache_hit() {
        val gateway = TestGateway(budget, account)

        // note: requesting the same budget / account twice to trigger a cache lookup
        val repo = TestRepo(listOf(
            BudgetAccountIds(budgetId = budgetId, accountId = accountId),
            BudgetAccountIds(budgetId = budgetId, accountId = accountId)
        ))

        val step = GetBudgetAccounts(repo, gateway)
        step.getBudgetAccounts() // makes service call

        assertEquals(gateway.getBudgetCallCount, 1)
    }

    @Test
    fun getBudgetAccounts__budget_not_found() {
        val gateway = TestGateway(budget, account)
        val repo = TestRepo(listOf(
            BudgetAccountIds(budgetId = "foo", accountId = accountId)
        ))

        assertThrows<IllegalArgumentException> {
            GetBudgetAccounts(repo, gateway).getBudgetAccounts()
        }
    }

    @Test
    fun getBudgetAccounts__account_not_found() {
        val gateway = TestGateway(budget, account)
        val repo = TestRepo(listOf(
            BudgetAccountIds(budgetId = budgetId, accountId = "bar")
        ))

        assertThrows<IllegalArgumentException> {
            GetBudgetAccounts(repo, gateway).getBudgetAccounts()
        }
    }

    private class TestGateway(private val budget: Budget, private val account: Account) : YnabBudgetAccountsGateway {
        private val budgetId = GetBudgetAccountsTest.budgetId
        private val accountId = GetBudgetAccountsTest.accountId
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
}