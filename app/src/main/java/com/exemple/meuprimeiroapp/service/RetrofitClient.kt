package com.exemple.meuprimeiroapp.service

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val Base_URL = "http://10.0.2.2:3000/" // Endereço para acessar o emulador Android

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(Base_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    }
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)

    }

}