package com.exemple.meuprimeiroapp

import android.app.Application
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.MapsInitializer.Renderer
import com.google.android.gms.maps.OnMapsSdkInitializedCallback

class MeuPrimeiroApplication : Application(), OnMapsSdkInitializedCallback {

    override fun onCreate() {
        super.onCreate()
        // Initialize Maps SDK at the application level to avoid repeated initialization
        // and potential database lock issues in Activities.
        MapsInitializer.initialize(applicationContext, Renderer.LATEST, this)
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
