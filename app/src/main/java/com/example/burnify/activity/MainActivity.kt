package com.example.burnify.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.burnify.App
import com.example.burnify.service.AccelerometerService
import com.example.burnify.service.GyroscopeService  // Importa il servizio del giroscopio
import com.example.burnify.viewmodel.AccelerometerViewModel
import com.example.burnify.viewmodel.GyroscopeViewModel  // Importa il ViewModel per il giroscopio

class MainActivity : ComponentActivity() {

    private val accelerometerViewModel: AccelerometerViewModel by viewModels()
    private val gyroscopeViewModel: GyroscopeViewModel by viewModels()  // ViewModel per il giroscopio

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Avvio esplicito del servizio per l'accelerometro
        val accelerometerServiceIntent = Intent(this, AccelerometerService::class.java)
        startService(accelerometerServiceIntent)

        // Avvio esplicito del servizio per il giroscopio
        val gyroscopeServiceIntent = Intent(this, GyroscopeService::class.java)  // Servizio per il giroscopio
        startService(gyroscopeServiceIntent)  // Avvia il servizio per il giroscopio

        setContent {
            App(accelerometerViewModel = accelerometerViewModel, gyroscopeViewModel = gyroscopeViewModel)  // Passa anche il ViewModel del giroscopio
        }
    }
}
