package com.pissiphany.bany.annotation

import com.squareup.moshi.JsonQualifier

/**
 * Based on process described here: https://medium.com/@naturalwarren/moshi-made-simple-jsonqualifier-b99559c826ad
 */

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@JsonQualifier
internal annotation class DataEnvelope(val wrappers: Int = 1)