package com.pissiphany.bany

import com.pissiphany.bany.adapter.BASE_URL
import com.pissiphany.bany.adapter.Constants.CONFIG_FILE
import com.pissiphany.bany.adapter.Constants.LAST_KNOWLEDGE_OF_SERVER_FILE
import com.pissiphany.bany.adapter.config.BanyConfig
import com.pissiphany.bany.adapter.controller.SyncTransactionsWithYnabController
import com.pissiphany.bany.adapter.factory.ThirdPartyTransactionGatewayFactoryImpl
import com.pissiphany.bany.adapter.gateway.YnabBudgetAccountsGatewayImpl
import com.pissiphany.bany.adapter.gateway.YnabMostRecentTransactionsGatewayImpl
import com.pissiphany.bany.adapter.gateway.YnabSaveTransactionsGatewayImpl
import com.pissiphany.bany.adapter.json.DataEnvelopeFactory
import com.pissiphany.bany.adapter.json.LocalDateAdapter
import com.pissiphany.bany.adapter.json.LocalDateTimeAdapter
import com.pissiphany.bany.adapter.mapper.*
import com.pissiphany.bany.plugin.BanyPlugin
import com.pissiphany.bany.adapter.presenter.Presenter
import com.pissiphany.bany.adapter.repository.FileBasedConfigurationRepository
import com.pissiphany.bany.adapter.repository.FileBasedLastKnowledgeOfServerRepository
import com.pissiphany.bany.adapter.service.RetrofitFactory
import com.pissiphany.bany.adapter.service.YnabService
import com.pissiphany.bany.adapter.view.ConsoleView
import com.pissiphany.bany.domain.useCase.SyncThirdPartyTransactionsUseCase
import com.pissiphany.bany.domain.useCase.step.GetBudgetAccounts
import com.pissiphany.bany.domain.useCase.step.GetMostRecentTransaction
import com.pissiphany.bany.domain.useCase.step.GetNewTransactions
import com.pissiphany.bany.domain.useCase.step.SaveNewTransactions
import com.squareup.moshi.Moshi
import org.pf4j.DefaultPluginManager
import java.lang.IllegalStateException

fun main() {
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
    val getBudgetAccounts = GetBudgetAccounts(configurationRepository, ynabBudgetAccountsGateway)

    val lastKnowledgeOfServerRepository = FileBasedLastKnowledgeOfServerRepository(LAST_KNOWLEDGE_OF_SERVER_FILE)
    val mostRecentTransactionsGateway = YnabMostRecentTransactionsGatewayImpl(ynabService, TransactionMapper())
    val getMostRecentTransaction = GetMostRecentTransaction(lastKnowledgeOfServerRepository, mostRecentTransactionsGateway)

    val saveTransactionsGateway = YnabSaveTransactionsGatewayImpl(ynabService, TransactionMapper())
    val saveNewTransactions = SaveNewTransactions(saveTransactionsGateway)

    val presenter = Presenter(ConsoleView())

    val pluginTransactionMapper = BanyPluginTransactionMapper()

    // plugins
    val pluginManager = DefaultPluginManager()
    val plugins = pluginManager.getExtensions(BanyPlugin::class.java)

    var initializedPlugins: List<BanyPlugin>? = null
    try {
        initializedPlugins = plugins
            .filter { plugin ->
                plugin.name in config.plugins
                        && plugin.setup(config.plugins.getValue(plugin.name))
            }

        if (initializedPlugins.isEmpty()) throw IllegalStateException("No enabled plugins found!")

        val gatewayFactory = ThirdPartyTransactionGatewayFactoryImpl(initializedPlugins, pluginTransactionMapper)
        val getNewTransactions = GetNewTransactions(gatewayFactory)

        val syncThirdPartyTransactionsUseCase = SyncThirdPartyTransactionsUseCase(
            getBudgetAccounts, getMostRecentTransaction, getNewTransactions, saveNewTransactions, presenter
        )

        SyncTransactionsWithYnabController(syncThirdPartyTransactionsUseCase).sync()

        // persist any changes to disk
        lastKnowledgeOfServerRepository.saveChanges()
    } finally {
        initializedPlugins?.forEach { plugin ->
            plugin.tearDown()
        }
    }

    // stop all active plugins
    pluginManager.stopPlugins()
}

