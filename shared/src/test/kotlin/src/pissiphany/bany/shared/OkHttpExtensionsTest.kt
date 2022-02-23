package src.pissiphany.bany.shared

import com.pissiphany.bany.shared.fetch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import okio.IOException
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@OptIn(ExperimentalCoroutinesApi::class)
class OkHttpExtensionsTest {
    private val client = OkHttpClient.Builder().build()
    private val request by lazy {
        Request.Builder()
            .get()
            .url(server.url("/"))
            .build()
    }

    private lateinit var server: MockWebServer

    @BeforeEach
    fun setup() {
        server = MockWebServer()
    }

    @AfterEach
    fun after() {
        server.shutdown()
    }

    @Test
    fun fetch() = runTest {
        server.enqueue(MockResponse().setBody("foo!"))
        server.start()

        val response = client.fetch(request)

        assertEquals(200, response.code)
    }

    @Test
    fun `fetch - throw on server error`() = runTest {
        server.enqueue(
            MockResponse()
                .setSocketPolicy(SocketPolicy.DISCONNECT_AT_START)
        )
        server.start()

        assertThrows<IOException> { client.fetch(request) }
    }
}