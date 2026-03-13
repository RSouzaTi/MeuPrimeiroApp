package com.exemple.meuprimeiroapp.ui

import CircleTransform
import android.widget.ImageView
import com.exemple.meuprimeiroapp.R
import com.squareup.picasso.Picasso

fun ImageView.loadUrl(url: String?) {
    if (url.isNullOrEmpty()) {
        setImageResource(R.drawable.ic_error)
        return
    }

    Picasso.get()
        .load(url)
        .placeholder(R.drawable.ic_download)
        .error(R.drawable.ic_error)
        .fit() // Ajusta a imagem ao tamanho da ImageView
        .centerCrop() // Corta para preencher mantendo a proporção
        // .transform(CircleTransform()) // Comentei para testar se a imagem aparece sem a transformação
        .into(this)
}
