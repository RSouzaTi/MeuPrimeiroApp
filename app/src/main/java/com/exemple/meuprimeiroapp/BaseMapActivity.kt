package com.exemple.meuprimeiroapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

abstract class BaseMapActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialization moved to MeuPrimeiroApplication to prevent redundant calls
        // and potential database lock issues.
    }
}
