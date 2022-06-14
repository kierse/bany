package com.pissiphany.bany.factory

import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object RetrofitFactory {
    fun create(
        baseUrl: String,
        token: String,
        moshi: Moshi,
        loggingLevel: HttpLoggingInterceptor.Level = HttpLoggingInterceptor.Level.NONE
    ): Retrofit {
        return Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(buildOkHttpClient(token, loggingLevel))
            .baseUrl(baseUrl)
            .build()
    }

    private fun buildOkHttpClient(token: String, level: HttpLoggingInterceptor.Level): OkHttpClient {
        val logging = HttpLoggingInterceptor()
            .apply {
                redactHeader("Authorization")
                redactHeader("Cookie")
                setLevel(level)
            }
        val builder = OkHttpClient.Builder()
            .addInterceptor(logging)
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