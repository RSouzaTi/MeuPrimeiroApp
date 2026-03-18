package com.exemple.meuprimeiroapp.model

import com.google.gson.annotations.SerializedName

data class Item(
    val id: String,
    val value: ItemValue // O segredo está aqui: os dados estão dentro de 'value'
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
) {
    val fullName: String
        get() = "$name $surname"
}

data class ItemLocation(
    val name: String,
    val latitude: Double,
    val longitude: Double
)