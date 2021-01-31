package com.pissiphany.bany

import com.pissiphany.bany.adapter.dataStructure.YnabConnection
import com.pissiphany.bany.adapter.dataStructure.YnabCredentials
import com.pissiphany.bany.dataStructure.ServiceConnection
import com.pissiphany.bany.dataStructure.ServiceCredentials
import com.pissiphany.bany.plugin.BanyConfigurablePlugin
import com.pissiphany.bany.plugin.BanyPlugin
import com.pissiphany.bany.plugin.BanyPluginFactory
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class BanyExtensionTest {
    @Test
    fun associateWithNotNull() {
        val selector = { i: Int ->
            if (i % 2 == 0) i.toString()
            else null
        }

        val expected = mapOf(2 to "2", 4 to "4")

        val result = listOf(1,2,3,4).associateWithNotNull(selector)

        assertEquals(expected, result)
    }

    @Test
    fun mapToYnabCredentials() {
        val connection = buildConnection()
        val connections = mapOf("budgetId" to listOf(connection))
        val credentials = buildCredentials(connections)

        val expected = buildYnabCredentials("budgetId", credentials)

        assertEquals(expected, mapToYnabCredentials(credentials))
    }

    @Test
    fun `mapToYnabCredentials - filter disabled connections`() {
        val connection1 = buildConnection(false)
        val connection2 = buildConnection(true)
        val connections = mapOf("budgetId" to listOf(connection1, connection2))
        val credentials = buildCredentials(connections)

        val expected = buildYnabCredentials("budgetId", credentials)
        val result = mapToYnabCredentials(credentials) ?: fail()

        assertEquals(expected, result)
        assertTrue(result.connections.size == 1)
    }

    @Test
    fun `mapToYnabCredentials - return null when no active connections`() {
        val connection = buildConnection(false)
        val connections = mapOf("budgetId" to listOf(connection))
        val credentials = buildCredentials(connections)

        assertNull(mapToYnabCredentials(credentials))
    }

    @Test
    fun buildMactoryMap() {

    }

    private fun buildConnection(enabled: Boolean = true) = ServiceConnection(
        name = "name",
        ynabAccountId = "ynabAccountId",
        thirdPartyAccountId = "thirdPartyAccountId",
        enabled = enabled,
        data = mapOf("a" to "b")
    )

    private fun buildCredentials(connections: Map<String, List<ServiceConnection>>) = ServiceCredentials(
        username = "username",
        password = "password",
        connections = connections,
        enabled = true,
        description = "description",
        data = mapOf("foo" to "bar")
    )

    private fun buildYnabCredentials(budgetId: String, credentials: ServiceCredentials): YnabCredentials {
        val connections = credentials.connections[budgetId]?.get(0)
            ?.let {
                listOf(
                    YnabConnection(
                        name = it.name,
                        ynabBudgetId = budgetId,
                        ynabAccountId = it.ynabAccountId,
                        thirdPartyAccountId = it.thirdPartyAccountId,
                        data = mapOf("a" to "b")
                    )
                )
            }
            ?: emptyList()

        return YnabCredentials(
            username = credentials.username,
            password = credentials.password,
            connections = connections,
            enabled = credentials.enabled,
            description = credentials.description,
            data = credentials.data
        )
    }
}