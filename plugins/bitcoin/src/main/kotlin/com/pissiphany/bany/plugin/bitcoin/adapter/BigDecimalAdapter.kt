package com.pissiphany.bany.plugin.bitcoin.adapter

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.ToJson
import java.math.BigDecimal

class BigDecimalAdapter {
    @FromJson
    fun fromJson(reader: JsonReader): BigDecimal {
        return if (reader.peek() == JsonReader.Token.NULL) {
            reader.nextNull<BigDecimal>()
            BigDecimal.ZERO
        } else {
            BigDecimal(reader.nextString())
        }
    }

    @ToJson
    fun toJson(value: BigDecimal): String {
        throw NotImplementedError("this shouldn't be used by cibc plugin!")
    }
}