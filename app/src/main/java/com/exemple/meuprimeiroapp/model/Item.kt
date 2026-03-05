package com.exemple.meuprimeiroapp.model

/**
 * Representa um item contendo informações detalhadas de um perfil de usuário.
 *
 * @property id O identificador único do item.
 * @property name O nome do usuário.
 * @property surname O sobrenome do usuário.
 * @property profession A profissão do usuário.
 * @property imageUrl A URL da imagem de perfil.
 * @property age A idade do usuário.
 * @property location Os dados de localização geográfica associados.
 */
data class Item(
    val id: Int,
    val name: String,
    val surname: String,
    val profession: String,
    val imageUrl: String,
    val age: Int,
    val location: ItemLocation
)

/**
 * Representa a localização geográfica de um item.
 *
 * @property name O nome do local ou cidade.
 * @property latitude A coordenada de latitude.
 * @property longitude A coordenada de longitude.
 */
data class ItemLocation(
    val name: String,
    val latitude: Double,
    val longitude: Double
)
