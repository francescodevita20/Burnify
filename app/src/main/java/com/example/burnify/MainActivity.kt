package com.example.burnify

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
class MainActivity : ComponentActivity() {

    private val accelerometerViewModel: AccelerometerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Avvio esplicito del servizio
        val serviceIntent = Intent(this, AccelerometerService::class.java)
        startService(serviceIntent)  // Assicurati che il servizio venga avviato

        setContent {
            App(accelerometerViewModel = accelerometerViewModel)
        }
    }
}
