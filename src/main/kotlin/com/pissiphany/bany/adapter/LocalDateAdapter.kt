package com.pissiphany.bany.adapter

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
    fun toJson(localDate: LocalDate): String {
        return localDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
    }
}