package com.pissiphany.bany

import com.pissiphany.bany.adapter.BASE_URL
import com.pissiphany.bany.adapter.Constants.CONFIG_FILE
import com.pissiphany.bany.adapter.Constants.LAST_KNOWLEDGE_OF_SERVER_FILE
import com.pissiphany.bany.config.BanyConfig
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
import com.pissiphany.bany.adapter.repository.ConfigurationRepositoryImpl
import com.pissiphany.bany.adapter.repository.FileBasedLastKnowledgeOfServerRepository
import com.pissiphany.bany.adapter.service.RetrofitFactory
import com.pissiphany.bany.adapter.service.YnabService
import com.pissiphany.bany.adapter.view.ConsoleView
import com.pissiphany.bany.domain.useCase.SyncThirdPartyTransactionsUseCase
import com.pissiphany.bany.domain.useCase.step.GetBudgetAccounts
import com.pissiphany.bany.domain.useCase.step.GetMostRecentTransaction
import com.pissiphany.bany.domain.useCase.step.GetNewTransactions
import com.pissiphany.bany.domain.useCase.step.SaveNewTransactions
import com.pissiphany.bany.plugin.BanyPluginFactory
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

    val configurationRepository = ConfigurationRepositoryImpl(config, BudgetAccountIdsMapper())
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
    val factoryMap = buildFactoryMap(pluginManager.getExtensions(BanyPluginFactory::class.java))

    val enabledPlugins = config.plugins
        .mapValues { (_, credentialList) ->
            credentialList.filter { it.enabled }
        }

    val initializedPlugins = mutableListOf<BanyPlugin>()
    try {
        enabledPlugins.forEach(
            fun(pluginName, credentialList) {
                // Note: above #mapValues call can lead to keys that point to empty lists
                if (credentialList.isEmpty()) return

                val factory = factoryMap[pluginName] ?: return // TODO log skipping this set of credentials

                for (credentials in credentialList) {
                    val plugin = factory.createPlugin(pluginName, credentials)
                    if (plugin.setup()) initializedPlugins.add(plugin)
                }
            }
        )

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
        initializedPlugins.forEach { it.tearDown() }
    }

    // stop all active plugins
    pluginManager.stopPlugins()
}

private fun buildFactoryMap(factories: List<BanyPluginFactory>): Map<String, BanyPluginFactory> {
    val map = mutableMapOf<String, BanyPluginFactory>()
    for (factory in factories) {
        for (pluginName in factory.pluginNames) {
            if (pluginName in map) {
                throw DuplicatePluginConfigurationException("multiple factories report a plugin named of '$pluginName'")
            }

            map[pluginName] = factory
        }
    }

    return map
}

private class DuplicatePluginConfigurationException(message: String) : Throwable(message)
