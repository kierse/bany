package com.pissiphany.bany

import com.pissiphany.bany.Constants.CONFIG_FILE
import com.pissiphany.bany.Constants.LAST_KNOWLEDGE_OF_SERVER_FILE
import com.pissiphany.bany.adapter.controller.SyncTransactionsWithYnabController
import com.pissiphany.bany.adapter.factory.ThirdPartyTransactionGatewayFactoryImpl
import com.pissiphany.bany.adapter.gateway.YnabAccountDetailsGatewayImpl
import com.pissiphany.bany.adapter.gateway.YnabSaveTransactionsGatewayImpl
import com.pissiphany.bany.adapter.mapper.*
import com.pissiphany.bany.adapter.presenter.Presenter
import com.pissiphany.bany.adapter.repository.ConfigurationRepositoryImpl
import com.pissiphany.bany.adapter.repository.PropertiesLastKnowledgeOfServerRepository
import com.pissiphany.bany.adapter.view.ConsoleView
import com.pissiphany.bany.dataStructure.BanyConfig
import com.pissiphany.bany.domain.useCase.SyncThirdPartyTransactionsUseCase
import com.pissiphany.bany.factory.DataEnvelopeFactory
import com.pissiphany.bany.adapter.OffsetDateTimeAdapter
import com.pissiphany.bany.adapter.dataStructure.YnabConnection
import com.pissiphany.bany.adapter.dataStructure.YnabCredentials
import com.pissiphany.bany.adapter.service.ThirdPartyTransactionService
import com.pissiphany.bany.dataStructure.ServiceCredentials
import com.pissiphany.bany.domain.useCase.step.*
import com.pissiphany.bany.mapper.RetrofitAccountMapper
import com.pissiphany.bany.mapper.RetrofitTransactionMapper
import com.pissiphany.bany.plugin.BanyPluginFactory
import com.pissiphany.bany.factory.RetrofitFactory
import com.pissiphany.bany.mapper.BanyPluginDataMapper
import com.pissiphany.bany.plugin.ConfigurablePlugin
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

    val enabledPlugins = config.plugins
        .mapValues { (_, credentialList) -> credentialList.filter(ServiceCredentials::enabled) }
        .filter { it.value.isNotEmpty() }
    if (enabledPlugins.isEmpty()) throw IllegalStateException("No enabled plugins found!")

    val credentialsMap = enabledPlugins
        .values
        .flatten()
        .associateWith(::mapToYnabCredentials)

    val serviceBuilder = RetrofitFactory.create(BASE_URL, config.ynabApiToken, moshi)
    val retrofitService = serviceBuilder.create(RetrofitYnabService::class.java)
    val ynabApiService = RetrofitYnabApiService(retrofitService, RetrofitAccountMapper(), RetrofitTransactionMapper())

    // Step2GetAccountDetails
    val lastKnowledgeOfServerRepository = PropertiesLastKnowledgeOfServerRepository(LAST_KNOWLEDGE_OF_SERVER_FILE)
    val accountDetailsGateway = YnabAccountDetailsGatewayImpl(
        ynabApiService, YnabBudgetAccountIdsMapper(), YnabAccountMapper(), YnabTransactionMapper()
    )
    val getAccountDetails = GetAccountDetails(lastKnowledgeOfServerRepository, accountDetailsGateway)

    // Step4ProcessNewTransaction
    val processNewTransaction = ProcessNewTransaction()

    // Step5SaveNewTransactions
    val saveTransactionsGateway = YnabSaveTransactionsGatewayImpl(
        ynabApiService, YnabBudgetAccountIdsMapper(), YnabTransactionMapper()
    )
    val saveNewTransactions = SaveNewTransactions(saveTransactionsGateway)

    val configurationRepository = ConfigurationRepositoryImpl(credentialsMap.values.toList(), YnabBudgetAccountIdsMapper())
    val presenter = Presenter(ConsoleView())

    // plugins
    val pluginManager = DefaultPluginManager()
    val factoryMap = buildFactoryMap(pluginManager.getExtensions(BanyPluginFactory::class.java))

    val initializedPlugins = mutableListOf<ConfigurablePlugin>()
    try {
        val initializedServices = mutableListOf<ThirdPartyTransactionService>()
        enabledPlugins.forEach(
            fun(pluginName, credentialList) {
                val factory = factoryMap[pluginName] ?: return // TODO log skipping this set of credentials

                for (credentials in credentialList) {
                    val plugin = factory.createPlugin(pluginName, credentialsMap.getValue(credentials))
                    if (plugin.setup()) {
                        initializedServices.add(ThirdPartyTransactionServiceImpl(plugin, BanyPluginDataMapper()))
                        initializedPlugins.add(plugin)
                    }
                }
            }
        )

        if (initializedServices.isEmpty()) throw IllegalStateException("No enabled plugins found!")

        // Step3GetNewTransactions
        val gatewayFactory = ThirdPartyTransactionGatewayFactoryImpl(
            initializedServices, YnabBudgetAccountIdsMapper(), YnabTransactionMapper()
        )
        val getNewTransactions = GetNewTransactions(gatewayFactory)

        val syncThirdPartyTransactionsUseCase = SyncThirdPartyTransactionsUseCase(
            configurationRepository,
            getAccountDetails,
            getNewTransactions,
            processNewTransaction,
            saveNewTransactions,
            presenter
        )

        SyncTransactionsWithYnabController(syncThirdPartyTransactionsUseCase).sync()

        // persist any changes to disk
        lastKnowledgeOfServerRepository.saveChanges()
    } finally {
        initializedPlugins.forEach(ConfigurablePlugin::tearDown)
    }

    // stop all active plugins
    pluginManager.stopPlugins()
}

// TODO test
private fun mapToYnabCredentials(credentials: ServiceCredentials): YnabCredentials {
    val connections = credentials.connections.map { connection ->
        with(connection) {
            YnabConnection(
                name = name,
                ynabBudgetId = ynabBudgetId,
                ynabAccountId = ynabAccountId,
                thirdPartyAccountId = thirdPartyAccountId
            )
        }
    }
    return YnabCredentials(
        username = credentials.username,
        password = credentials.password,
        connections = connections,
        data = credentials.data,
        enabled = credentials.enabled,
        description = credentials.description
    )
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
