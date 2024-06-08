package com.android.kotlin.familymessagingapp.data.remote.client_retrofit

import android.app.Application
import com.chuckerteam.chucker.api.ChuckerInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class AppApiService(application: Application) {

    private val BASE_URL = "https://developer.android.com/"

    private val DURATION_TIMEOUT = 60

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(DURATION_TIMEOUT.toLong(), TimeUnit.SECONDS)
        .readTimeout(DURATION_TIMEOUT.toLong(), TimeUnit.SECONDS)
        .addInterceptor(ChuckerInterceptor(application))
        .build()

    @JvmField
    val retrofit: AppService = Retrofit.Builder()
//        .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .build()
        .create(AppService::class.java)
}