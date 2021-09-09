package com.pissiphany.bany.plugin.equitable.client

import com.pissiphany.bany.plugin.BanyPlugin
import java.math.BigDecimal

interface EquitableClient {
    fun createSession(
        username: String,
        password: String,
        securityQuestions: Map<String, String>
    ): EquitableClientSession

    interface EquitableClientSession {
        fun terminateSession()
        fun isValid(): Boolean
        fun checkSession()
        fun getInsuranceDetails(connection: BanyPlugin.Connection): InsuranceDetails
        fun getInvestmentDetails(connection: BanyPlugin.Connection): InvestmentDetails

        data class InsuranceDetails(
            val loanAvailable: BigDecimal,
            val loanBalance: BigDecimal
        )

        data class InvestmentDetails(
            val totalDeposits: BigDecimal,
            val totalWithdrawals: BigDecimal,
            val netDeposits: BigDecimal,
            val marketValue: BigDecimal,
        )
    }
}

