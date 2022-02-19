package com.pissiphany.bany.plugin.equitable.client

import com.pissiphany.bany.shared.logger
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import org.jsoup.Connection
import org.jsoup.nodes.Element
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.nio.charset.Charset
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

private const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.132 Safari/537.36"

internal fun URL.addToPath(vararg parts: String) = URL(this, parts.joinToString("/")).toString()

internal fun Connection.execute(expected: Int = 200, err: (code: Int, msg: String) -> String): Connection.Response {
    return this
        .userAgent(USER_AGENT)
        .execute()
        .also { res ->
            check(res.statusCode() == expected) { err(res.statusCode(), res.statusMessage()) }
        }
}

internal fun List<Connection.KeyVal>.toRequestBody() =
    joinToString("&") { keyVal -> "${keyVal.key()}=${keyVal.value()}" }.toRequestBody()

internal fun Request.Builder.cookies(cookies: Cookies): Request.Builder = apply {
    addHeader("Cookie", cookies.joinToString(";"))
}

internal suspend fun OkHttpClient.fetch(request: Request): Response = suspendCoroutine { cont ->
    newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            cont.resumeWithException(e)
        }

        override fun onResponse(call: Call, response: Response) {
            cont.resume(response)
        }
    })
}

internal suspend fun <T> Response.process(processor: suspend (bodyStream: InputStream, charset: Charset) -> T?): T? = use { resp ->
    val logger by logger()
    if (!resp.isSuccessful) {
        logger.warn("Request unsuccessful: (HTTP ${resp.code}) ${resp.message}")
        return null
    }

    val responseBody = resp.body
    if (responseBody == null) {
        logger.warn("Response has empty body!")
        return null
    }

    return with(responseBody) {
        processor(byteStream(), contentType()?.charset() ?: Charsets.UTF_8)
    }
}

internal fun Response.cookies(): Cookies = headers.values("Set-Cookie")

internal var Element.value: String
    get() = `val`()
    set(new) { `val`(new) }