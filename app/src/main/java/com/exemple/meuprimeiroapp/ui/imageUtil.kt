package com.exemple.meuprimeiroapp.ui

import android.widget.ImageView
import com.exemple.meuprimeiroapp.R
import com.squareup.picasso.Picasso

fun ImageView.loadUrl(url: String?) {
    if (url.isNullOrBlank()) {
        setImageResource(R.drawable.ic_error)
        return
    }

    // Log para você conferir a URL no Logcat
    android.util.Log.d("Picasso", "URL da Imagem: $url")

    Picasso.get()
        .load(url)
        .placeholder(R.drawable.ic_download)
        .error(R.drawable.ic_error)
        // Removido o fit() e centerCrop() temporariamente para garantir que a imagem apareça
        // independente do tamanho da ImageView no XML
        .into(this)
}