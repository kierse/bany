package com.pissiphany.bany.service

import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object RetrofitFactory {
    fun create(
        baseUrl: String,
        token: String,
        moshi: Moshi
    ): Retrofit {
        return Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(buildOkHttpClient(token))
            .baseUrl(baseUrl)
            .build()
    }

    private fun buildOkHttpClient(token: String): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .addInterceptor(
                fun(chain): Response {
                    val request = chain.request()
                        .newBuilder()
                        .header("Authorization", "Bearer $token")
                        .build()

                    return chain.proceed(request)
                }
            )


        return builder.build()
    }
}