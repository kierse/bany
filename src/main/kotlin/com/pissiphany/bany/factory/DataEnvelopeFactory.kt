package com.pissiphany.bany.factory

import com.pissiphany.bany.annotation.DataEnvelope
import com.squareup.moshi.*
import java.lang.UnsupportedOperationException
import java.lang.reflect.Type

/**
 * Based on process described here: https://medium.com/@naturalwarren/moshi-made-simple-jsonqualifier-b99559c826ad
 */

class DataEnvelopeFactory : JsonAdapter.Factory {
    override fun create(type: Type, annotations: MutableSet<out Annotation>, moshi: Moshi): JsonAdapter<*>? {
        val delegateAnnotations = Types.nextAnnotations(annotations, DataEnvelope::class.java) ?: return null
        val delegate = moshi.nextAdapter<Any>(this, type, delegateAnnotations)

        val annotation = annotations.find { it is DataEnvelope } as? DataEnvelope

        return DataEnvelopeJsonAdapter(delegate, annotation?.wrappers ?: 1)
    }

    private class DataEnvelopeJsonAdapter(private val delegate: JsonAdapter<*>, private val wrappers: Int): JsonAdapter<Any>() {
        override fun fromJson(reader: JsonReader): Any? {
            // iterating {level} times allows me to eat up level numbers of wrappers in the
            // response json
            for (i in 0 until wrappers) {
                reader.beginObject()
                reader.nextName()
            }

            val envelope = delegate.fromJson(reader)

            // Note: be sure to end {level} objects as well!
            for (i in 0 until wrappers) {
                reader.endObject()
            }

            return envelope
        }

        override fun toJson(writer: JsonWriter, value: Any?) =
                throw UnsupportedOperationException("@DataEnvelope is only used to deserialize objects!")
    }
}