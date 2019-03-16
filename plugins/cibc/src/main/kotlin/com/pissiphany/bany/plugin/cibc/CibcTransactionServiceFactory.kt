package com.pissiphany.bany.plugin.cibc

import com.pissiphany.bany.plugin.BanyPlugin
import com.pissiphany.bany.plugin.BanyPluginFactory
import com.pissiphany.bany.plugin.cibc.environment.CibcEnvironment
import com.pissiphany.bany.plugin.cibc.environment.SimpliiEnvironment
import com.pissiphany.bany.plugin.cibc.mapper.CibcTransactionMapper
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import org.pf4j.Extension
import java.lang.IllegalArgumentException
import java.net.CookieManager
import java.net.CookiePolicy

internal const val CIBC = "cibc"
internal const val SIMPLII = "simplii"

@Extension
class CibcTransactionServiceFactory : BanyPluginFactory {
    override val pluginNames = setOf(CIBC, SIMPLII)

    private val moshi = Moshi.Builder().build()

    override fun createPlugin(pluginName: String, credentials: BanyPlugin.Credentials): BanyPlugin {
        val env = when (pluginName) {
            CIBC -> CibcEnvironment()
            SIMPLII -> SimpliiEnvironment()
            else -> throw IllegalArgumentException("unknown/unsupported plugin '$pluginName'")
        }

        val jar = QuotePreservingCookieJar(CookieManager(null, CookiePolicy.ACCEPT_ALL))
        val client = OkHttpClient
            .Builder()
            .cookieJar(jar)
            .build()

        return CibcTransactionService(
            credentials, env, moshi, client, CibcTransactionMapper()
        )
    }
}