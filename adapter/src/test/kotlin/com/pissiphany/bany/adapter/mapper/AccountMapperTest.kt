package com.pissiphany.bany.adapter.mapper

import com.pissiphany.bany.adapter.dataStructure.YnabAccount
import com.pissiphany.bany.domain.dataStructure.Account
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import java.lang.IllegalArgumentException

internal class AccountMapperTest {
    @Test
    fun toAccount__checking() {
        val ynabAccount = YnabAccount("id", "name", false, 10L, "checking")
        assertEquals(Account("id", "name", 10L, false, Account.Type.CHECKING), AccountMapper().toAccount(ynabAccount))
    }

    @Test
    fun toAccount__savings() {
        val ynabAccount = YnabAccount("id", "name", false, 10L, "savings")
        assertEquals(Account("id", "name", 10L, false, Account.Type.SAVINGS), AccountMapper().toAccount(ynabAccount))
    }

    @Test
    fun toAccount__cash() {
        val ynabAccount = YnabAccount("id", "name", false, 10L, "cash")
        assertEquals(Account("id", "name", 10L, false, Account.Type.CASH), AccountMapper().toAccount(ynabAccount))
    }

    @Test
    fun toAccount__credit_card() {
        val ynabAccount = YnabAccount("id", "name", false, 10L, "creditCard")
        assertEquals(Account("id", "name", 10L, false, Account.Type.CREDIT_CARD), AccountMapper().toAccount(ynabAccount))
    }

    @Test
    fun toAccount__line_of_credit() {
        val ynabAccount = YnabAccount("id", "name", false, 10L, "lineOfCredit")
        assertEquals(Account("id", "name", 10L, false, Account.Type.LINE_OF_CREDIT), AccountMapper().toAccount(ynabAccount))
    }

    @Test
    fun toAccount__other_asset() {
        val ynabAccount = YnabAccount("id", "name", false, 10L, "otherAsset")
        assertEquals(Account("id", "name", 10L, false, Account.Type.OTHER_ASSET), AccountMapper().toAccount(ynabAccount))
    }

    @Test
    fun toAccount__other_liability() {
        val ynabAccount = YnabAccount("id", "name", false, 10L, "otherLiability")
        assertEquals(Account("id", "name", 10L, false, Account.Type.OTHER_LIABILITY), AccountMapper().toAccount(ynabAccount))
    }

    @Test
    fun toAccount__pay_pal() {
        val ynabAccount = YnabAccount("id", "name", false, 10L, "payPal")
        assertEquals(Account("id", "name", 10L, false, Account.Type.PAY_PAL), AccountMapper().toAccount(ynabAccount))
    }

    @Test
    fun toAccount__merchant_account() {
        val ynabAccount = YnabAccount("id", "name", false, 10L, "merchantAccount")
        assertEquals(Account("id", "name", 10L, false, Account.Type.MERCHANT_ACCOUNT), AccountMapper().toAccount(ynabAccount))
    }

    @Test
    fun toAccount__investment_account() {
        val ynabAccount = YnabAccount("id", "name", false, 10L, "investmentAccount")
        assertEquals(Account("id", "name", 10L, false, Account.Type.INVESTMENT_ACCOUNT), AccountMapper().toAccount(ynabAccount))
    }

    @Test
    fun toAccount__mortgage() {
        val ynabAccount = YnabAccount("id", "name", false, 10L, "mortgage")
        assertEquals(Account("id", "name", 10L, false, Account.Type.MORTGAGE), AccountMapper().toAccount(ynabAccount))
    }

    @Test
    fun toAccount__unknown_type() {
        val ynabAccount = YnabAccount("id", "name", false, 10L, "foo")
        assertThrows<IllegalArgumentException> { AccountMapper().toAccount(ynabAccount) }
    }
}