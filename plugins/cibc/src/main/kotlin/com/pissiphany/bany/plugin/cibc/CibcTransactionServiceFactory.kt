package com.pissiphany.bany.plugin.cibc

import com.pissiphany.bany.plugin.BanyPlugin
import com.pissiphany.bany.plugin.BanyPluginFactory
import com.pissiphany.bany.plugin.cibc.environment.CibcEnvironment
import com.pissiphany.bany.plugin.cibc.mapper.CibcTransactionMapper
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import org.pf4j.Extension
import java.lang.IllegalArgumentException

private const val CIBC = "cibc"

@Extension
class CibcTransactionServiceFactory : BanyPluginFactory {
    override val pluginNames = setOf(CIBC)

    override fun createPlugin(pluginName: String, credentials: BanyPlugin.Credentials): BanyPlugin {
        val env = when (pluginName) {
            CIBC -> CibcEnvironment()
            else -> throw IllegalArgumentException("unknown/unsupported plugin '$pluginName'")
        }

        return CibcTransactionService(
            credentials, env, Moshi.Builder().build(), OkHttpClient(), CibcTransactionMapper()
        )
    }
}