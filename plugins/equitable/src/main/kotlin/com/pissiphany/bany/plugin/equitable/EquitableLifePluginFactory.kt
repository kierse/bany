package com.pissiphany.bany.plugin.equitable

import com.pissiphany.bany.plugin.BanyConfigurablePlugin
import com.pissiphany.bany.plugin.BanyPlugin
import com.pissiphany.bany.plugin.BanyPluginFactory
import com.pissiphany.bany.plugin.equitable.client.EquitableClientImpl
import com.pissiphany.bany.plugin.equitable.client.OkHttpWrapper
import com.pissiphany.bany.plugin.equitable.client.OkHttpWrapperImpl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import org.jsoup.Jsoup
import org.pf4j.Extension
import java.io.InputStream
import java.nio.charset.Charset

internal const val EQUITABLE_ROOT = "https://client.equitable.ca"
internal const val EQUITABLE_LIFE = "equitable-life"

@Extension
class EquitableLifePluginFactory : BanyPluginFactory {
    override val pluginNames = setOf(EQUITABLE_LIFE)

    private val client = lazy {
        OkHttpClient
            .Builder()
            .followRedirects(false)
            .build()
    }

    private val clientWrapper: OkHttpWrapper = OkHttpWrapperImpl(client) { bodyStream: InputStream, charset: Charset ->
        Jsoup.parse(bodyStream, charset.name(), root.toString())
    }

    private val root = EQUITABLE_ROOT.toHttpUrl()

    override suspend fun createPlugin(pluginName: String, credentials: BanyPlugin.Credentials): BanyConfigurablePlugin {
        val client = EquitableClientImpl(clientWrapper, root)
        return EquitableLifePlugin(client, credentials)
    }
}