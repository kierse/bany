package com.pissiphany.bany

import com.pissiphany.bany.Constants.CONFIG_FILE
import com.pissiphany.bany.Constants.LAST_KNOWLEDGE_OF_SERVER_FILE
import com.pissiphany.bany.adapter.controller.SyncTransactionsWithYnabController
import com.pissiphany.bany.adapter.factory.ThirdPartyTransactionGatewayFactoryImpl
import com.pissiphany.bany.adapter.gateway.YnabBudgetAccountsGatewayImpl
import com.pissiphany.bany.adapter.gateway.YnabMostRecentTransactionsGatewayImpl
import com.pissiphany.bany.adapter.gateway.YnabSaveTransactionsGatewayImpl
import com.pissiphany.bany.adapter.mapper.*
import com.pissiphany.bany.adapter.presenter.Presenter
import com.pissiphany.bany.adapter.repository.ConfigurationRepositoryImpl
import com.pissiphany.bany.adapter.repository.PropertiesLastKnowledgeOfServerRepository
import com.pissiphany.bany.adapter.view.ConsoleView
import com.pissiphany.bany.dataStructure.BanyConfig
import com.pissiphany.bany.domain.useCase.SyncThirdPartyTransactionsUseCase
import com.pissiphany.bany.domain.useCase.step.GetBudgetAccounts
import com.pissiphany.bany.domain.useCase.step.GetMostRecentTransaction
import com.pissiphany.bany.domain.useCase.step.GetNewTransactions
import com.pissiphany.bany.domain.useCase.step.SaveNewTransactions
import com.pissiphany.bany.factory.DataEnvelopeFactory
import com.pissiphany.bany.adapter.OffsetDateTimeAdapter
import com.pissiphany.bany.mapper.RetrofitAccountMapper
import com.pissiphany.bany.mapper.RetrofitBudgetMapper
import com.pissiphany.bany.mapper.RetrofitTransactionMapper
import com.pissiphany.bany.plugin.BanyPluginFactory
import com.pissiphany.bany.factory.RetrofitFactory
import com.pissiphany.bany.service.RetrofitYnabService
import com.pissiphany.bany.service.RetrofitYnabApiService
import com.pissiphany.bany.service.ThirdPartyTransactionServiceImpl
import com.squareup.moshi.Moshi
import org.pf4j.DefaultPluginManager
import java.lang.IllegalStateException

fun main() {
    val moshi = Moshi.Builder()
        .add(DataEnvelopeFactory())
        .add(OffsetDateTimeAdapter())
        .build()

    val adapter = moshi.adapter(BanyConfig::class.java)
    val config = adapter.fromJson(CONFIG_FILE.readText()) ?:
            throw UnknownError("Unable to parse and instantiate application config!")

    val serviceBuilder = RetrofitFactory.create(BASE_URL, config.ynabApiToken, moshi)
    val retrofitService = serviceBuilder.create(RetrofitYnabService::class.java)
    val ynabApiService = RetrofitYnabApiService(
        retrofitService, RetrofitBudgetMapper(), RetrofitAccountMapper(), RetrofitTransactionMapper()
    )

    val configurationRepository = ConfigurationRepositoryImpl(config.plugins, YnabBudgetAccountIdsMapper())
    val ynabBudgetAccountsGateway = YnabBudgetAccountsGatewayImpl(ynabApiService, YnabBudgetMapper(), YnabAccountMapper())
    val getBudgetAccounts = GetBudgetAccounts(configurationRepository, ynabBudgetAccountsGateway)

    val lastKnowledgeOfServerRepository = PropertiesLastKnowledgeOfServerRepository(LAST_KNOWLEDGE_OF_SERVER_FILE)
    val mostRecentTransactionsGateway = YnabMostRecentTransactionsGatewayImpl(
        ynabApiService, YnabBudgetMapper(), YnabAccountMapper(), YnabTransactionMapper()
    )
    val getMostRecentTransaction = GetMostRecentTransaction(lastKnowledgeOfServerRepository, mostRecentTransactionsGateway)

    val saveTransactionsGateway = YnabSaveTransactionsGatewayImpl(ynabApiService, YnabBudgetMapper(), YnabTransactionMapper())
    val saveNewTransactions = SaveNewTransactions(saveTransactionsGateway)

    val presenter = Presenter(ConsoleView())

    // plugins
    val pluginManager = DefaultPluginManager()
    val factoryMap = buildFactoryMap(pluginManager.getExtensions(BanyPluginFactory::class.java))

    val enabledPlugins = config.plugins
        .mapValues { (_, credentialList) ->
            credentialList.filter { it.enabled }
        }

    val initializedServices = mutableListOf<ThirdPartyTransactionServiceImpl>()
    try {
        enabledPlugins.forEach(
            fun(pluginName, credentialList) {
                val factory = factoryMap[pluginName] ?: return // TODO log skipping this set of credentials

                for (credentials in credentialList) {
                    val plugin = factory.createPlugin(pluginName, credentials)
                    if (plugin.setup()) {
                        initializedServices.add(
                            ThirdPartyTransactionServiceImpl(plugin, BanyPluginTransactionMapper())
                        )
                    }
                }
            }
        )

        if (initializedServices.isEmpty()) throw IllegalStateException("No enabled plugins found!")

        val gatewayFactory = ThirdPartyTransactionGatewayFactoryImpl(initializedServices, YnabTransactionMapper())
        val getNewTransactions = GetNewTransactions(gatewayFactory)

        val syncThirdPartyTransactionsUseCase = SyncThirdPartyTransactionsUseCase(
            getBudgetAccounts, getMostRecentTransaction, getNewTransactions, saveNewTransactions, presenter
        )

        SyncTransactionsWithYnabController(syncThirdPartyTransactionsUseCase).sync()

        // persist any changes to disk
        lastKnowledgeOfServerRepository.saveChanges()
    } finally {
        initializedServices.forEach { it.plugin.tearDown() }
    }

    // stop all active plugins
    pluginManager.stopPlugins()
}

private fun buildFactoryMap(factories: List<BanyPluginFactory>): Map<String, BanyPluginFactory> {
    val map = mutableMapOf<String, BanyPluginFactory>()
    for (factory in factories) {
        for (pluginName in factory.pluginNames) {
            if (pluginName in map) {
                throw DuplicatePluginConfigurationException("multiple factories report a plugin named '$pluginName'")
            }

            map[pluginName] = factory
        }
    }

    return map
}

private class DuplicatePluginConfigurationException(message: String) : Throwable(message)
