package com.exemple.meuprimeiroapp.model

import com.google.gson.annotations.SerializedName

/**
 * Representa um item contendo informações detalhadas de um perfil de usuário.
 */
data class Item(
    val id: Int,
    val value: ItemValue,
)


data class ItemValue(
    val id: String,
    val name: String,
    val surname: String,
    val profession: String,
    @SerializedName("imageUrl")
    val imageUrl: String,
    val age: Int,
    val location: ItemLocation?
){
    val fullName: String
        get() = "$name $surname"
}

/**
 * Representa a localização geográfica de um item.
 */
data class ItemLocation(
    val name: String,
    val latitude: Double,
    val longitude: Double
)
