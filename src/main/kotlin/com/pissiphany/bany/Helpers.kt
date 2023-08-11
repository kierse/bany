package com.pissiphany.bany

import com.pissiphany.bany.adapter.dataStructure.YnabConnection
import com.pissiphany.bany.adapter.dataStructure.YnabCredentials
import com.pissiphany.bany.configApi.ServiceCredentials
import com.pissiphany.bany.plugin.BanyPluginFactory
import com.pissiphany.bany.plugin.PluginName

internal fun <K,V> List<K>.associateWithNotNull(valueSelector: (K) -> V?): Map<K,V> {
    val result = mutableMapOf<K,V>()
    for (key in this) {
        val value = valueSelector(key) ?: continue
        result[key] = value
    }

    return result
}

internal fun mapToYnabCredentials(credentials: ServiceCredentials): YnabCredentials? {
    val connections = mutableListOf<YnabConnection>()
    for ((budgetId, serviceConnections) in credentials.connections) {
        for (serviceConnection in serviceConnections) {
            if (!serviceConnection.enabled) continue
            connections += with(serviceConnection) {
                YnabConnection(
                    name = name,
                    ynabBudgetId = budgetId,
                    ynabAccountId = ynabAccountId,
                    thirdPartyAccountId = thirdPartyAccountId,
                    data = data
                )
            }
        }
    }

    if (connections.isEmpty()) return null

    return YnabCredentials(
        username = credentials.username,
        password = credentials.password,
        connections = connections,
        data = credentials.data,
        enabled = credentials.enabled,
        description = credentials.description
    )
}

internal fun buildFactoryMap(factories: List<BanyPluginFactory>): Map<PluginName, BanyPluginFactory> {
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

internal fun Map<PluginName, List<ServiceCredentials>>.filterDisabledPlugins(): Map<PluginName, List<ServiceCredentials>> {
    return mapValues { (_, credentialList) -> credentialList.filter(ServiceCredentials::enabled) }
        .filter { it.value.isNotEmpty() }
}