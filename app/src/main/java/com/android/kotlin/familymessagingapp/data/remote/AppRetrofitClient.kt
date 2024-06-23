package com.android.kotlin.familymessagingapp.data.remote

import android.app.Application
import com.android.kotlin.familymessagingapp.BuildConfig
import com.android.kotlin.familymessagingapp.data.remote.client_retrofit.BackendApiService
import com.android.kotlin.familymessagingapp.utils.Constant
import com.chuckerteam.chucker.api.ChuckerInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class AppRetrofitClient(private val application: Application) {

    val instance: BackendApiService get() = run {
        val okHttpClient =  OkHttpClient.Builder()
            .connectTimeout(Constant.DURATION_TIMEOUT.toLong(), TimeUnit.SECONDS)
            .readTimeout(Constant.DURATION_TIMEOUT.toLong(), TimeUnit.SECONDS)
        if (BuildConfig.DEBUG) okHttpClient.addInterceptor(ChuckerInterceptor(application))
        return@run Retrofit.Builder()
//        .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(Constant.BASE_URL)
            .client(okHttpClient.build())
            .build()
            .create(BackendApiService::class.java)
    }
}