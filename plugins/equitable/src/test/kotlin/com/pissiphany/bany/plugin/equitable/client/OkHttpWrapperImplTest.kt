package com.pissiphany.bany.plugin.equitable.client

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import okio.IOException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.io.InputStream
import java.nio.charset.Charset

private val html = """
    <html>
        <head>
            <title>Hello world!</title>
        </head>
        <body>
            <p>Hello world!</p>
        </body>
    </html>
""".trimIndent()

@OptIn(ExperimentalCoroutinesApi::class)
class OkHttpWrapperImplTest {
    private lateinit var server: MockWebServer

    private val client = lazy {
        OkHttpClient
            .Builder()
            .followRedirects(false)
            .build()
    }

    private val jsoupProcessor: suspend (InputStream, Charset) -> Document? = { stream, charset ->
        Jsoup.parse(stream, charset.name(), server.url("/").toString())
    }

    private val request: Request
        get() = Request.Builder()
            .url(server.url("/"))
            .get()
            .build()

    @BeforeEach
    fun before() {
        server = MockWebServer()
    }

    @AfterEach
    fun after() {
        server.shutdown()
    }

    @ParameterizedTest(name = "fetchAndProcess - request failed: ${ParameterizedTest.ARGUMENTS_WITH_NAMES_PLACEHOLDER}")
    @EnumSource(
        value = SocketPolicy::class,
        names = ["DISCONNECT_AT_START"]
    )
    fun `fetchAndProcess - request failed`(failure: SocketPolicy) = runTest {
        server.enqueue(MockResponse().setSocketPolicy(failure))
        server.start()

        assertNull(createWrapper().fetchAndProcess(request))
    }

    @Test
    fun `fetchAndProcess - request unsuccessful`() = runTest {
        server.enqueue(MockResponse().setResponseCode(404))
        server.start()

        assertNull(createWrapper().fetchAndProcess(request))
    }

    @Test
    fun `fetchAndProcess - unable to parse getLogOn page response`() = runTest {
        server.enqueue(MockResponse())
        server.start()

        val wrapper = createWrapper { _, _ ->
            throw IOException("foo!")
        }

        // simulate failed attempt to process Response
        assertNull(wrapper.fetchAndProcess(request))
    }

    @Test
    fun fetchAndProcess() = runTest {
        server.enqueue(
            MockResponse()
                .setBody(html)
                .addHeader("Set-Cookie", "cookie1=value")
                .addHeader("Set-Cookie", "cookie2=value")
        )
        server.start()

        val result = createWrapper().fetchAndProcess(request) ?: fail()

        assertEquals("Hello world!", result.document.selectFirst("title").text())
        assertEquals(
            listOf("cookie1=value", "cookie2=value"),
            result.cookies
        )
    }
    
    @ParameterizedTest(name = "fetchRedirectCookies - request failed: ${ParameterizedTest.ARGUMENTS_WITH_NAMES_PLACEHOLDER}")
    @EnumSource(
        value = SocketPolicy::class,
        names = ["DISCONNECT_AT_START"]
    )
    fun `fetchRedirectCookies - request failed`(failure: SocketPolicy) = runTest {
        server.enqueue(MockResponse().setSocketPolicy(failure))
        server.start()

        assertTrue(createWrapper().fetchRedirectCookies(request).isEmpty())
    }

    @Test
    fun `fetchRedirectCookies - request unsuccessful`() = runTest {
        server.enqueue(MockResponse().setResponseCode(404))
        server.start()

        assertTrue(createWrapper().fetchRedirectCookies(request).isEmpty())
    }
    
    @Test
    fun fetchRedirectCookies() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(302)
                .addHeader("Set-Cookie", "cookie1=value")
                .addHeader("Set-Cookie", "cookie2=value")
        )
        server.start()

        val result = createWrapper().fetchRedirectCookies(request)

        assertEquals(
            listOf("cookie1=value", "cookie2=value"),
            result
        )
    }

    private fun createWrapper(
        processor: suspend (bodyStream: InputStream, charset: Charset) -> Document? = jsoupProcessor
    ): OkHttpWrapper = OkHttpWrapperImpl(client, processor)
}