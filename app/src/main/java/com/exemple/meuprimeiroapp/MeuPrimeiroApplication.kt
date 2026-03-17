package com.exemple.meuprimeiroapp

import android.app.Application
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.MapsInitializer.Renderer
import com.google.android.gms.maps.OnMapsSdkInitializedCallback

class MeuPrimeiroApplication : Application(), OnMapsSdkInitializedCallback {

    override fun onCreate() {
        super.onCreate()
        // Initialize Maps SDK with LEGACY renderer to avoid internal SDK errors
        // such as errorCode: 65561, engine: 2 (LATEST)
        MapsInitializer.initialize(applicationContext, Renderer.LEGACY, this)
    }

    override fun onMapsSdkInitialized(renderer: Renderer) {
        when (renderer) {
            Renderer.LATEST -> {
                // Modern renderer initialized
            }
            Renderer.LEGACY -> {
                // Legacy renderer initialized
            }
        }
    }
}
