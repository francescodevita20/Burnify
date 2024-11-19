package com.example.burnify.activity

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.burnify.App
import com.example.burnify.scheduleDatabaseCleanup // Assicurati che questa funzione sia importata correttamente
import com.example.burnify.service.AccelerometerService
import com.example.burnify.service.GyroscopeService
import com.example.burnify.service.MagnetometerService
import com.example.burnify.viewmodel.AccelerometerViewModel
import com.example.burnify.viewmodel.GyroscopeViewModel
import com.example.burnify.viewmodel.MagnetometerViewModel

class MainActivity : ComponentActivity() {

    private val accelerometerViewModel: AccelerometerViewModel by viewModels()
    private val gyroscopeViewModel: GyroscopeViewModel by viewModels()
    private val magnetometerViewModel: MagnetometerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mostra il dialogo che chiede all'utente di disabilitare l'ottimizzazione della batteria
        showBatteryOptimizationDialog()

        // Avvia i servizi di accelerometro, giroscopio e magnetometro in modalità foreground
        startSensorServices()

        // Imposta il contenuto dell'app con i ViewModel
        setContent {
            App(
                accelerometerViewModel = accelerometerViewModel,
                gyroscopeViewModel = gyroscopeViewModel,
                magnetometerViewModel = magnetometerViewModel
            )
        }
    }

    private fun showBatteryOptimizationDialog() {
        // Verifica se l'app è già esclusa dall'ottimizzazione della batteria
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        val isIgnoringBatteryOptimizations = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            powerManager.isIgnoringBatteryOptimizations(packageName)
        } else {
            true // Su versioni precedenti di Android non è necessario fare questa verifica
        }

        if (isIgnoringBatteryOptimizations) {
            // Se l'app è già esclusa dall'ottimizzazione, non mostrare il dialogo
            return
        }

        // Crea un dialogo che informa l'utente solo se l'ottimizzazione non è stata disabilitata
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

    private fun startSensorServices() {
        // Avvia il servizio accelerometro in foreground
        val accelerometerServiceIntent = Intent(this, AccelerometerService::class.java)
        startForegroundService(accelerometerServiceIntent)

        // Avvia il servizio giroscopio in foreground
        val gyroscopeServiceIntent = Intent(this, GyroscopeService::class.java)
        startForegroundService(gyroscopeServiceIntent)

        // Avvia il servizio magnetometro in foreground
        val magnetometerServiceIntent = Intent(this, MagnetometerService::class.java)
        startForegroundService(magnetometerServiceIntent)
    }
}
