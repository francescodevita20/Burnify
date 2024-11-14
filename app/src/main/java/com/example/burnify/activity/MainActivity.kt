package com.example.burnify.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.burnify.App
import com.example.burnify.service.AccelerometerService
import com.example.burnify.service.GyroscopeService
import com.example.burnify.service.MagnetometerService // Importa il servizio del magnetometro
import com.example.burnify.viewmodel.AccelerometerViewModel
import com.example.burnify.viewmodel.GyroscopeViewModel
import com.example.burnify.viewmodel.MagnetometerViewModel // Importa il ViewModel per il magnetometro

class MainActivity : ComponentActivity() {

    private val accelerometerViewModel: AccelerometerViewModel by viewModels()
    private val gyroscopeViewModel: GyroscopeViewModel by viewModels()
    private val magnetometerViewModel: MagnetometerViewModel by viewModels() // ViewModel per il magnetometro

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Avvio esplicito del servizio per l'accelerometro
        val accelerometerServiceIntent = Intent(this, AccelerometerService::class.java)
        startService(accelerometerServiceIntent)

        // Avvio esplicito del servizio per il giroscopio
        val gyroscopeServiceIntent = Intent(this, GyroscopeService::class.java)
        startService(gyroscopeServiceIntent)

        // Avvio esplicito del servizio per il magnetometro
        val magnetometerServiceIntent = Intent(this, MagnetometerService::class.java) // Servizio per il magnetometro
        startService(magnetometerServiceIntent)  // Avvia il servizio per il magnetometro

        setContent {
            App(
                accelerometerViewModel = accelerometerViewModel,
                gyroscopeViewModel = gyroscopeViewModel,
                magnetometerViewModel = magnetometerViewModel // Passa anche il ViewModel del magnetometro
            )
        }
    }
}
