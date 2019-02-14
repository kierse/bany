package com.pissiphany.bany.adapter.json

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class LocalDateAdapter {
    @FromJson
    fun fromJson(raw: String): LocalDate {
        return LocalDate.parse(raw, DateTimeFormatter.ISO_LOCAL_DATE)
    }

    @ToJson
    fun toJson(date: LocalDate): String {
        return date.format(DateTimeFormatter.ISO_LOCAL_DATE)
    }
}