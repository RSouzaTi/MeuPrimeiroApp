package com.exemple.meuprimeiroapp.service

import com.exemple.meuprimeiroapp.model.Item
import retrofit2.http.GET
import retrofit2.http.Path


interface ItemApiService {

    @GET("items")
    suspend fun getItems(): List<Item>

    @GET("items/{id}")
    suspend fun getItem(@Path("id") id: String): Item
    }



