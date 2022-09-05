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
import com.pissiphany.bany.adapter.LocalDateAdapter
import com.pissiphany.bany.adapter.OffsetDateTimeAdapter
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
import kotlinx.coroutines.*
import mu.KotlinLogging
import org.pf4j.DefaultPluginManager

fun main() = runBlocking {
    val logger = KotlinLogging.logger {}

    val moshi = Moshi.Builder()
        .add(DataEnvelopeFactory())
        .add(LocalDateAdapter())
        .add(OffsetDateTimeAdapter())
        .build()

    val adapter = moshi.adapter(BanyConfig::class.java)

    logger.info { "Parsing config file at: ${CONFIG_FILE.absoluteFile}" }
    val config = checkNotNull(adapter.fromJson(CONFIG_FILE.readText())) {
        "Unable to parse and instantiate application config!"
    }

    val enabledPlugins = config.plugins.filterDisabledPlugins()
    check(enabledPlugins.isNotEmpty()) { "No enabled plugins found!" }

    logger.info { "Found plugin configuration for: ${enabledPlugins.keys.sorted().joinToString(", ")}" }

    val credentialsMap: Map<ServiceCredentials, YnabCredentials> = enabledPlugins
        .values
        .flatten()
        .associateWithNotNull(::mapToYnabCredentials)
    check(credentialsMap.isNotEmpty()) { "No enabled credentials found!" }

    val serviceBuilder = RetrofitFactory.create(BASE_URL, config.ynabApiToken, moshi)
    val retrofitService = serviceBuilder.create(RetrofitYnabService::class.java)
    val ynabApiService = RetrofitYnabApiService(retrofitService, RetrofitAccountMapper(), RetrofitTransactionMapper())

    // Step1GetAccountDetails
    val lastKnowledgeOfServerRepository = PropertiesLastKnowledgeOfServerRepository(this, LAST_KNOWLEDGE_OF_SERVER_FILE)
    val accountDetailsGateway = YnabAccountDetailsGatewayImpl(
        ynabApiService, YnabBudgetAccountIdsMapper(), YnabAccountMapper(), YnabTransactionMapper()
    )
    val getAccountDetails = GetAccountDetails(lastKnowledgeOfServerRepository, accountDetailsGateway)

    // Step3ProcessNewTransaction
    val processNewTransaction = ProcessNewTransaction()

    // Step4SaveNewTransactions
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
        val deferredServices = supervisorScope {
            buildList {
                for ((pluginName, serviceCredentialsList) in enabledPlugins) {
                    val factory = factoryMap[pluginName]
                    if (factory == null) {
                        logger.warn { "Unable to find plugin factory for '$pluginName', skipping!" }
                        continue
                    }

                    for (serviceCredentials in serviceCredentialsList) {
                        val deferred = async {
                            val plugin = factory.createPlugin(pluginName, credentialsMap.getValue(serviceCredentials))
                            if (plugin.setup()) {
                                logger.debug { "Initialized '$pluginName' plugin: '${serviceCredentials.description}'" }
                                ThirdPartyTransactionServiceImpl(plugin, BanyPluginDataMapper())
                            } else {
                                logger.info("Skipping service credentials '${serviceCredentials.description}'. Failed to setup plugin")
                                null
                            }
                        }
                        add(deferred)
                    }
                }
            }
        }

        val initializedServices = deferredServices
            .fold(mutableListOf<ThirdPartyTransactionService>()) { acc, cur ->
                try {
                    val service = cur.await()
                    if (service != null) acc += service
                } catch (e: Throwable) {
                    logger.error("Exception while configuring plugin: ${e.message}")
                }
                acc
            }

        // Step2GetNewTransactions
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
        logger.info("Tearing down plugins")
        val jobs = supervisorScope {
            initializedPlugins
                .map { plugin -> launch { plugin.tearDown() } }
        }
        jobs.joinAll()
    }

    // stop all active plugins
    logger.info("Stopping plugins...")
    pluginManager.stopPlugins()
    logger.info("Done!")
}
