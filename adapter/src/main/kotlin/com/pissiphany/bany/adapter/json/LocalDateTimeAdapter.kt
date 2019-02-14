package com.pissiphany.bany.adapter.json

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class LocalDateTimeAdapter {
    @FromJson
    fun fromJson(raw: String): LocalDateTime {
        return LocalDateTime.parse(raw, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    }

    @ToJson
    fun toJson(timestamp: LocalDateTime): String {
        return timestamp.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    }
}