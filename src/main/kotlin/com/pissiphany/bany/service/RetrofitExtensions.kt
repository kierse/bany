package com.pissiphany.bany.service

import com.pissiphany.bany.shared.logger
import retrofit2.Response

fun <T> Response<T>.process(): T? {
    val logger by logger()
    if (!isSuccessful) {
        logger.warn("Request unsuccessful: (HTTP ${code()}) ${message()}")
        return null
    }

    return body()
}