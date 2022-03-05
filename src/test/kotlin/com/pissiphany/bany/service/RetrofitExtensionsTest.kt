package com.pissiphany.bany.service

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import okio.IOException
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET

@OptIn(ExperimentalCoroutinesApi::class)
class RetrofitExtensionsTest {
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

        val response = service().test().fetch()

        assertEquals(200, response.code())
        assertEquals("foo!", response.body())
    }

    @Test
    fun `fetch - throws exception`() = runTest {
        server.enqueue(
            MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START)
        )
        server.start()

        assertThrows<IOException> { service().test().fetch() }
    }

    private fun service(): TestApi {
        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
        return retrofit.create(TestApi::class.java)
    }

    interface TestApi {
        @GET("/")
        fun test(): Call<String>
    }
}