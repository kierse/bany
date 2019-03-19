package com.pissiphany.bany.plugin.cibc.adapter

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class OffsetDateTimeAdapter {
    @FromJson
    fun fromJson(raw: String): OffsetDateTime {
        return OffsetDateTime.parse(raw, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    }

    @ToJson
    fun toJson(timestamp: OffsetDateTime): String {
        return timestamp.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    }
}