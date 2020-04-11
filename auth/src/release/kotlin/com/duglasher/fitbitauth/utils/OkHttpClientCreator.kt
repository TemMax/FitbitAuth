package com.duglasher.fitbitauth.utils

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit


object OkHttpClientCreator {

    fun create() = OkHttpClient.Builder().apply {
        connectTimeout(30, TimeUnit.SECONDS)
        readTimeout(30, TimeUnit.SECONDS)
        writeTimeout(30, TimeUnit.SECONDS)
    }.build()

}