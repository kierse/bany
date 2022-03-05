package com.pissiphany.bany.service

import com.pissiphany.bany.shared.logger
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

suspend fun <T> Call<T>.fetch(): Response<T> = suspendCoroutine { cont ->
    enqueue(object : Callback<T> {
        override fun onFailure(call: Call<T>, t: Throwable) {
            cont.resumeWithException(t)
        }

        override fun onResponse(call: Call<T>, response: Response<T>) {
            cont.resume(response)
        }
    })
}

fun <T> Response<T>.process(): T? {
    val logger by logger()
    if (!isSuccessful) {
        logger.warn("Request unsuccessful: (HTTP ${code()}) ${message()}")
        return null
    }

    return body()
}