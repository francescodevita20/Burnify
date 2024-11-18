package com.example.burnify.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
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
    private val magnetometerViewModel: MagnetometerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mostra il dialogo che chiede all'utente di disabilitare l'ottimizzazione della batteria
        showBatteryOptimizationDialog()

        // Altri servizi e logica di avvio dell'app
        val accelerometerServiceIntent = Intent(this, AccelerometerService::class.java)
        startForegroundService(accelerometerServiceIntent)

        val gyroscopeServiceIntent = Intent(this, GyroscopeService::class.java)
        startForegroundService(gyroscopeServiceIntent)

        val magnetometerServiceIntent = Intent(this, MagnetometerService::class.java)
        startForegroundService(magnetometerServiceIntent)

        setContent {
            App(
                accelerometerViewModel = accelerometerViewModel,
                gyroscopeViewModel = gyroscopeViewModel,
                magnetometerViewModel = magnetometerViewModel
            )
        }
    }

    private fun showBatteryOptimizationDialog() {
        // Crea un dialogo che informa l'utente
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Disabilita Ottimizzazione Batteria")
            .setMessage("Per permettere all'app di funzionare correttamente in background, devi disabilitare l'ottimizzazione della batteria. Vuoi andare alle impostazioni?")
            .setPositiveButton("Sì") { _, _ ->
                // Se l'utente accetta, apri le impostazioni
                val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                startActivity(intent)
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss() // Chiude il dialogo
            }
            .setCancelable(false) // Il dialogo non può essere cancellato senza interazione
        builder.show()
    }
}