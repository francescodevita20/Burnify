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
import com.example.burnify.service.AccelerometerService
import com.example.burnify.service.GyroscopeService
import com.example.burnify.service.MagnetometerService
import com.example.burnify.util.setSharedPreferences
import com.example.burnify.util.getSharedPreferences
import com.example.burnify.viewmodel.AccelerometerViewModel
import com.example.burnify.viewmodel.GyroscopeViewModel
import com.example.burnify.viewmodel.MagnetometerViewModel
import com.example.burnify.viewmodel.PredictedActivityViewModel

/**
 * MainActivity is the entry point of the app where sensor services are initialized and the UI is set.
 */
class MainActivity : ComponentActivity() {

    // ViewModels for the sensors and predicted activity
    private val accelerometerViewModel: AccelerometerViewModel by viewModels()
    private val gyroscopeViewModel: GyroscopeViewModel by viewModels()
    private val magnetometerViewModel: MagnetometerViewModel by viewModels()
    private val predictedActivityViewModel: PredictedActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Show dialog asking the user to disable battery optimization if necessary
        showBatteryOptimizationDialog()

        // Start foreground services for accelerometer, gyroscope, and magnetometer
        startSensorServices()

        // Set the content of the app with the ViewModels
        setContent {
            App(
                accelerometerViewModel = accelerometerViewModel,
                gyroscopeViewModel = gyroscopeViewModel,
                magnetometerViewModel = magnetometerViewModel,
                predictedActivityViewModel = predictedActivityViewModel
            )
        }
    }

    /**
     * Shows a dialog asking the user to disable battery optimization if it's not already disabled.
     */
    private fun showBatteryOptimizationDialog() {
        // Get the power manager to check battery optimization settings
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        val isIgnoringBatteryOptimizations = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            powerManager.isIgnoringBatteryOptimizations(packageName)
        } else {
            true // No need to check for versions below Android M
        }

        if (isIgnoringBatteryOptimizations) {
            // If the app is already excluded from battery optimization, no need to show the dialog
            return
        }

        // Create and show an alert dialog to inform the user to disable battery optimization
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Disable Battery Optimization")
            .setMessage("To ensure the app works properly in the background, you need to disable battery optimization. Do you want to go to settings?")
            .setPositiveButton("Yes") { _, _ ->
                // If user accepts, open battery optimization settings
                val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                startActivity(intent)
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss() // Close the dialog if the user declines
            }
            .setCancelable(false) // Make the dialog non-cancelable without interaction
        builder.show()
    }

    /**
     * Starts foreground services for accelerometer, gyroscope, and magnetometer.
     */
    private fun startSensorServices() {
        // Start the accelerometer service in the foreground
        val accelerometerServiceIntent = Intent(this, AccelerometerService::class.java).apply {
            putExtra("workingmode", (getSharedPreferences(applicationContext, "setting")?.get("workingmode")).toString()) // Pass working mode to the service
        }
        startForegroundService(accelerometerServiceIntent)

        // Start the gyroscope service in the foreground
        val gyroscopeServiceIntent = Intent(this, GyroscopeService::class.java).apply {
            putExtra("workingmode", (getSharedPreferences(applicationContext, "setting")?.get("workingmode")).toString()) // Pass working mode to the service
        }
        startForegroundService(gyroscopeServiceIntent)

        // Start the magnetometer service in the foreground
        val magnetometerServiceIntent = Intent(this, MagnetometerService::class.java).apply {
            putExtra("workingmode", (getSharedPreferences(applicationContext, "setting")?.get("workingmode")).toString()) // Pass working mode to the service
        }
        startForegroundService(magnetometerServiceIntent)
    }

    /**
     * Retrieves settings from SharedPreferences or sets default values if no settings are found.
     */
    private fun getSettings() {
        val settingsMap = getSharedPreferences(applicationContext, "settings")

        if (settingsMap == null || settingsMap.isEmpty() || settingsMap.containsKey("sampling rate")) {
            // If no settings found or sampling rate is missing, set default values
            val defaultMap = mapOf("sampling rate" to 0.5)
            setSharedPreferences(applicationContext, defaultMap, "settings")
            println("Settings not found, default value set.")
        } else {
            // If settings are found, log and use the retrieved settings
            println("Settings retrieved successfully. Sampling rate: ${settingsMap["sampling rate"]}")
        }
    }
}
