package com.example.burnify.activity

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.ParcelUuid
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.example.burnify.App
import com.example.burnify.scheduleDatabaseCleanup // Assicurati che questa funzione sia importata correttamente
import com.example.burnify.service.AccelerometerService
import com.example.burnify.service.GyroscopeService
import com.example.burnify.service.MagnetometerService
import com.example.burnify.viewmodel.AccelerometerViewModel
import com.example.burnify.viewmodel.GyroscopeViewModel
import com.example.burnify.viewmodel.MagnetometerViewModel
import java.util.UUID

class MainActivity : ComponentActivity() {

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bleScanner: BluetoothLeScanner
    private val bluetoothPermissions = arrayOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    private lateinit var permissionsLauncher: ActivityResultLauncher<Array<String>>

    private val accelerometerViewModel: AccelerometerViewModel by viewModels()
    private val gyroscopeViewModel: GyroscopeViewModel by viewModels()
    private val magnetometerViewModel: MagnetometerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Register permissions launcher
        permissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allGranted = permissions.all { it.value }
            if (allGranted) {
                Log.d("Permissions", "All permissions granted")
                startBleScan() // Start BLE scan when permissions are granted
            } else {
                Log.d("Permissions", "Some permissions were denied")
            }
        }

        // Initialize Bluetooth Adapter
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        // Mostra il dialogo che chiede all'utente di disabilitare l'ottimizzazione della batteria
        showBatteryOptimizationDialog()

        // Avvia i servizi di accelerometro, giroscopio e magnetometro in modalità foreground
        startSensorServices()

        // Request permissions if not granted
        requestPermissions()

        // Initialize BLE Scanner
        bleScanner = bluetoothAdapter.bluetoothLeScanner

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

    private fun requestPermissions() {
        permissionsLauncher.launch(bluetoothPermissions)
    }

    private fun startBleScan() {
        val hasPermissions = bluetoothPermissions.all {
            checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED
        }

        if (!hasPermissions) {
            Log.d("Permissions", "Permissions not granted. Cannot start BLE scan.")
            return
        }

        val scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                    Log.d("BLE", "Device found: ${result.device.name}, ${result.device.address}")
                }
            }
        }

        bleScanner.startScan(scanCallback)
    }
}
