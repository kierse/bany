package com.pissiphany.bany.adapter.config

import com.pissiphany.bany.adapter.json.DataEnvelopeFactory
import com.pissiphany.bany.adapter.json.LocalDateTimeAdapter
import com.squareup.moshi.Moshi
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.io.File

internal class BanyConfigTest {
    companion object {
        private lateinit var moshi: Moshi

        @BeforeAll
        @JvmStatic
        internal fun setup() {
            moshi = Moshi.Builder()
                .add(DataEnvelopeFactory())
                .add(LocalDateTimeAdapter())
                .build()
        }
    }

    @Test
    fun config() {
        val adapter = moshi.adapter(BanyConfig::class.java)
        val json = File(System.getProperty("user.home") + "/.bany.config").readText()

        val config = adapter.fromJson(json) ?: fail("Unable to load config file!")

        config.ynabApiToken
    }
}