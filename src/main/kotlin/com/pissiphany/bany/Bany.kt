package com.pissiphany.bany

import com.pissiphany.bany.adapter.BASE_URL
import com.pissiphany.bany.adapter.Constants.CONFIG_FILE
import com.pissiphany.bany.adapter.Constants.LAST_KNOWLEDGE_OF_SERVER_FILE
import com.pissiphany.bany.adapter.config.BanyConfig
import com.pissiphany.bany.adapter.controller.SyncTransactionsWithYnabController
import com.pissiphany.bany.adapter.gateway.YnabBudgetAccountsGatewayImpl
import com.pissiphany.bany.adapter.gateway.YnabMostRecentTransactionsGatewayImpl
import com.pissiphany.bany.adapter.gateway.YnabSaveTransactionsGatewayImpl
import com.pissiphany.bany.adapter.json.DataEnvelopeFactory
import com.pissiphany.bany.adapter.json.LocalDateAdapter
import com.pissiphany.bany.adapter.json.LocalDateTimeAdapter
import com.pissiphany.bany.adapter.mapper.AccountMapper
import com.pissiphany.bany.adapter.mapper.BudgetAccountIdsMapper
import com.pissiphany.bany.adapter.mapper.BudgetMapper
import com.pissiphany.bany.adapter.mapper.TransactionMapper
import com.pissiphany.bany.adapter.repository.FileBasedConfigurationRepository
import com.pissiphany.bany.adapter.repository.FileBasedLastKnowledgeOfServerRepository
import com.pissiphany.bany.adapter.service.RetrofitFactory
import com.pissiphany.bany.adapter.service.YnabService
import com.pissiphany.bany.domain.dataStructure.Account
import com.pissiphany.bany.domain.dataStructure.Transaction
import com.pissiphany.bany.domain.gateway.ThirdPartyTransactionGateway
import com.pissiphany.bany.domain.useCase.budgetAccounts.GetBudgetAccountsUseCase
import com.pissiphany.bany.domain.useCase.thirdPartyTransactions.GetNewTransactionsUseCase
import com.pissiphany.bany.domain.useCase.ynabTransactions.GetMostRecentUseCase
import com.pissiphany.bany.domain.useCase.ynabTransactions.SaveTransactionsUseCase
import com.squareup.moshi.Moshi
import java.time.LocalDate

fun main(args: Array<String>) {
    val moshi = Moshi.Builder()
        .add(DataEnvelopeFactory())
        .add(LocalDateTimeAdapter())
        .add(LocalDateAdapter())
        .build()

    val adapter = moshi.adapter(BanyConfig::class.java)
    val config = adapter.fromJson(CONFIG_FILE.readText()) ?:
            throw UnknownError("Unable to parse and instantiate application config!")

    val serviceBuilder = RetrofitFactory.create(BASE_URL, config.ynabApiToken, moshi)
    val ynabService = serviceBuilder.create(YnabService::class.java)

    val configurationRepository = FileBasedConfigurationRepository(config, BudgetAccountIdsMapper())
    val ynabBudgetAccountsGateway = YnabBudgetAccountsGatewayImpl(ynabService, BudgetMapper(), AccountMapper())
    val budgetAccountsUseCase = GetBudgetAccountsUseCase(configurationRepository, ynabBudgetAccountsGateway)

    val lastKnowledgeOfServerRepository = FileBasedLastKnowledgeOfServerRepository(LAST_KNOWLEDGE_OF_SERVER_FILE)
    val mostRecentTransactionsGateway = YnabMostRecentTransactionsGatewayImpl(ynabService, TransactionMapper())
    val recentTransactionsUseCase = GetMostRecentUseCase(lastKnowledgeOfServerRepository, mostRecentTransactionsGateway)

    val newTransactionsWithYnabController = GetNewTransactionsUseCase(
        listOf(DummyThirdPartyTransactionGateway(config.plugins[0].connections[0].ynabAccountId))
    )

    val saveTransactionsGateway = YnabSaveTransactionsGatewayImpl(ynabService, TransactionMapper())
    val saveTransactionsUseCase = SaveTransactionsUseCase(saveTransactionsGateway)

    SyncTransactionsWithYnabController(
        budgetAccountsUseCase, recentTransactionsUseCase, newTransactionsWithYnabController, saveTransactionsUseCase
    ).sync()

    // persist any changes to disk
    lastKnowledgeOfServerRepository.saveChanges()
}

private class DummyThirdPartyTransactionGateway(accountId: String) : ThirdPartyTransactionGateway {
    override val account = Account(accountId, "name", 10L, false, Account.Type.CHECKING)

    override fun getNewTransactionSince(date: LocalDate?): List<Transaction> {
        return emptyList()
    }
}
