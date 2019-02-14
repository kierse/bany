package com.pissiphany.bany.adapter.json

import com.pissiphany.bany.adapter.annotation.DataEnvelope
import com.squareup.moshi.*
import java.lang.UnsupportedOperationException
import java.lang.reflect.Type

class DataEnvelopeFactory : JsonAdapter.Factory {
    override fun create(type: Type, annotations: MutableSet<out Annotation>, moshi: Moshi): JsonAdapter<*>? {
        val delegateAnnotations = Types.nextAnnotations(annotations, DataEnvelope::class.java) ?: return null
        val delegate = moshi.nextAdapter<Any>(this, type, delegateAnnotations)
        return DataEnvelopeJsonAdapter(delegate)
    }

    private class DataEnvelopeJsonAdapter(private val delegate: JsonAdapter<*>): JsonAdapter<Any>() {
        override fun fromJson(reader: JsonReader): Any? {
            reader.beginObject()
            reader.nextName()
            val envelope = delegate.fromJson(reader)
            reader.endObject()
            return envelope
        }

        override fun toJson(writer: JsonWriter, value: Any?) =
                throw UnsupportedOperationException("@DataEnvelope is only used to deserialize objects!")
    }
}