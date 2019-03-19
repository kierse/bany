package com.pissiphany.bany.adapter.mapper

import com.pissiphany.bany.adapter.dataStructure.YnabAccount
import com.pissiphany.bany.domain.dataStructure.Account
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import java.lang.IllegalArgumentException

internal class YnabAccountMapperTest {
    @Test
    fun toAccount__checking() {
        val ynabAccount = YnabAccount("id", "name", false, 1000, "checking")
        assertEquals(Account("id", "name", 100, false, Account.Type.CHECKING), YnabAccountMapper().toAccount(ynabAccount))
    }

    @Test
    fun toAccount__savings() {
        val ynabAccount = YnabAccount("id", "name", false, 1000, "savings")
        assertEquals(Account("id", "name", 100, false, Account.Type.SAVINGS), YnabAccountMapper().toAccount(ynabAccount))
    }

    @Test
    fun toAccount__cash() {
        val ynabAccount = YnabAccount("id", "name", false, 1000, "cash")
        assertEquals(Account("id", "name", 100, false, Account.Type.CASH), YnabAccountMapper().toAccount(ynabAccount))
    }

    @Test
    fun toAccount__credit_card() {
        val ynabAccount = YnabAccount("id", "name", false, 1000, "creditCard")
        assertEquals(Account("id", "name", 100, false, Account.Type.CREDIT_CARD), YnabAccountMapper().toAccount(ynabAccount))
    }

    @Test
    fun toAccount__line_of_credit() {
        val ynabAccount = YnabAccount("id", "name", false, 1000, "lineOfCredit")
        assertEquals(Account("id", "name", 100, false, Account.Type.LINE_OF_CREDIT), YnabAccountMapper().toAccount(ynabAccount))
    }

    @Test
    fun toAccount__other_asset() {
        val ynabAccount = YnabAccount("id", "name", false, 1000, "otherAsset")
        assertEquals(Account("id", "name", 100, false, Account.Type.OTHER_ASSET), YnabAccountMapper().toAccount(ynabAccount))
    }

    @Test
    fun toAccount__other_liability() {
        val ynabAccount = YnabAccount("id", "name", false, 1000, "otherLiability")
        assertEquals(Account("id", "name", 100, false, Account.Type.OTHER_LIABILITY), YnabAccountMapper().toAccount(ynabAccount))
    }

    @Test
    fun toAccount__pay_pal() {
        val ynabAccount = YnabAccount("id", "name", false, 1000, "payPal")
        assertEquals(Account("id", "name", 100, false, Account.Type.PAY_PAL), YnabAccountMapper().toAccount(ynabAccount))
    }

    @Test
    fun toAccount__merchant_account() {
        val ynabAccount = YnabAccount("id", "name", false, 1000, "merchantAccount")
        assertEquals(Account("id", "name", 100, false, Account.Type.MERCHANT_ACCOUNT), YnabAccountMapper().toAccount(ynabAccount))
    }

    @Test
    fun toAccount__investment_account() {
        val ynabAccount = YnabAccount("id", "name", false, 1000, "investmentAccount")
        assertEquals(Account("id", "name", 100, false, Account.Type.INVESTMENT_ACCOUNT), YnabAccountMapper().toAccount(ynabAccount))
    }

    @Test
    fun toAccount__mortgage() {
        val ynabAccount = YnabAccount("id", "name", false, 1000, "mortgage")
        assertEquals(Account("id", "name", 100, false, Account.Type.MORTGAGE), YnabAccountMapper().toAccount(ynabAccount))
    }

    @Test
    fun toAccount__unknown_type() {
        val ynabAccount = YnabAccount("id", "name", false, 1000, "foo")
        assertThrows<IllegalArgumentException> { YnabAccountMapper().toAccount(ynabAccount) }
    }
}