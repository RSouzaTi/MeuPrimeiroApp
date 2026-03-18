package com.exemple.meuprimeiroapp.service

import ItemApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val Base_URL = "http://10.0.2.2:3000/"

    private val logging = HttpLoggingInterceptor().apply {
        // AJUSTE: Mudar para BODY para conseguirmos ver o JSON no Logcat
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(Base_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val itemApiService: ItemApiService by lazy {
        retrofit.create(ItemApiService::class.java)
    }
}
