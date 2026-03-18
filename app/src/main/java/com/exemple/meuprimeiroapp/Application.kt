package com.example.meuprimeiroapp

import android.app.Application
import com.example.meuprimeiroapp.database.DatabaseBuilder

class Application : Application() {

    override fun onCreate() {
        super.onCreate()
        init()
    }

    private fun init() {
        DatabaseBuilder.getInstance(this)
    }
}