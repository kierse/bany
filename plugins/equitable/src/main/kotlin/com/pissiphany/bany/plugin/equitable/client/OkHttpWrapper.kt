package com.pissiphany.bany.plugin.equitable.client

import com.pissiphany.bany.shared.fetch
import com.pissiphany.bany.shared.logger
import okhttp3.*
import org.jsoup.nodes.Document
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset

interface OkHttpWrapper {
    suspend fun fetchAndProcess(request: Request): ResponseData?
    suspend fun fetchRedirectCookies(request: Request): Cookies

    data class ResponseData(val document: Document, val cookies: Cookies)
}

internal class OkHttpWrapperImpl(
    private val client: Lazy<OkHttpClient>,
    private val processor: suspend (bodyStream: InputStream, charset: Charset) -> Document?
) : OkHttpWrapper {
    private val logger by logger()

    override suspend fun fetchAndProcess(request: Request): OkHttpWrapper.ResponseData? {
        val response = try {
            client.value.fetch(request)
        } catch (e: IOException) {
            logger.warn("Request failed: ${e.message}")
            return null
        }

        val document = try {
            process(response, processor)
        } catch (e: IOException) {
            logger.warn("Unable to parse response: ${e.message}")
            null
        } ?: return null

        return OkHttpWrapper.ResponseData(document, response.cookies())
    }

    private suspend fun process(
        response: Response,
        processor: suspend (bodyStream: InputStream, charset: Charset) -> Document?
    ): Document? = response.use { resp ->
        if (!resp.isSuccessful) {
            logger.warn("Request unsuccessful: (HTTP ${resp.code}) ${resp.message}")
            return null
        }

        val responseBody = resp.body
            ?: throw IllegalStateException("Null ResponseBody! Was the response passed to CallBack.onResponse?")
        return with(responseBody) {
            processor(byteStream(), contentType()?.charset() ?: Charsets.UTF_8)
        }
    }

    override suspend fun fetchRedirectCookies(request: Request): Cookies {
        val response = try {
            client.value.fetch(request)
        } catch (e: IOException) {
            logger.warn("Request failed: ${e.message}")
            return emptyList()
        }

        response.use { resp ->
            if (!resp.isRedirect) {
                logger.warn("Request unsuccessful: (HTTP ${resp.code}) ${resp.message}")
                return emptyList()
            }

            return resp.cookies()
        }
    }
}
