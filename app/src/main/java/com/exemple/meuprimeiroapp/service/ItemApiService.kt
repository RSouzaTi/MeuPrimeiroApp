package com.exemple.meuprimeiroapp.service

import com.exemple.meuprimeiroapp.model.Item
import retrofit2.http.GET

interface ItemApiService {
    @GET("items")
    suspend fun getItems(): List<Item>
}


