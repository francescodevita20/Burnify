package com.example.burnify.activity

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.databinding.library.BuildConfig
import com.example.burnify.App
import com.example.burnify.service.UnifiedSensorService
import com.example.burnify.util.SensorDataManager
import com.example.burnify.util.clearSharedPreferences
import com.example.burnify.util.setSharedPreferences
import com.example.burnify.util.getSharedPreferences
import com.example.burnify.viewmodel.AccelerometerViewModel
import com.example.burnify.viewmodel.GyroscopeViewModel
import com.example.burnify.viewmodel.LastPredictionViewModel
import com.example.burnify.viewmodel.MagnetometerViewModel
import com.example.burnify.viewmodel.PredictedActivityViewModel

/**
 * MainActivity is the entry point of the app where the unified sensor service is initialized and the UI is set.
 */
class MainActivity : ComponentActivity() {

    // ViewModels for the sensors and predicted activity
    private val accelerometerViewModel: AccelerometerViewModel by viewModels()
    private val gyroscopeViewModel: GyroscopeViewModel by viewModels()
    private val magnetometerViewModel: MagnetometerViewModel by viewModels()
    private val predictedActivityViewModel: PredictedActivityViewModel by viewModels()
    private val lastPredictionViewModel: LastPredictionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("MainActivity", "onCreate called")

        // Retrieve user data from SharedPreferences using the correct key
        val userData = getSharedPreferences(applicationContext, "userdata", "user_data_key")

        // Only navigate to OnboardingActivity if user data is missing
        if (userData == null || !userData.contains("weight") || !userData.contains("height") || !userData.contains("age")) {
            Log.d("MainActivity", "User data missing, navigating to OnboardingActivity")
            val intent = Intent(this, OnboardingActivity::class.java)
            startActivity(intent)
        }

        // Initialize ViewModel
        SensorDataManager.lastPredictionViewModel = lastPredictionViewModel

        // Show battery optimization dialog if required
        showBatteryOptimizationDialog()

        // Start the sensor service
        startUnifiedSensorService()

        // Set content for Compose UI
        setContent {
            App(
                accelerometerViewModel = accelerometerViewModel,
                gyroscopeViewModel = gyroscopeViewModel,
                magnetometerViewModel = magnetometerViewModel,
                predictedActivityViewModel = predictedActivityViewModel,
                lastPredictionViewModel = lastPredictionViewModel
            )
        }
    }

    /**
     * Starts the unified sensor service in the foreground.
     */
    private fun startUnifiedSensorService() {
        Log.d("MainActivity", "Starting UnifiedSensorService")

        // Retrieve the working mode and pass it to the service
        val workingMode = getSharedPreferences(applicationContext, "workingmode", "working_mode_key")?.get("workingmode")

        val unifiedServiceIntent = Intent(this, UnifiedSensorService::class.java).apply {
            putExtra("workingmode", workingMode.toString())
        }
        startForegroundService(unifiedServiceIntent)
    }

    /**
     * Shows a dialog asking the user to disable battery optimization if it's not already disabled.
     */
    private fun showBatteryOptimizationDialog() {
        Log.d("MainActivity", "Checking battery optimization settings")
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        val isIgnoringBatteryOptimizations = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            powerManager.isIgnoringBatteryOptimizations(packageName)
        } else {
            true // No need to check for versions below Android M
        }

        if (isIgnoringBatteryOptimizations) {
            Log.d("MainActivity", "Battery optimization already disabled")
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
     * Retrieves settings from SharedPreferences or sets default values if no settings are found.
     */
    private fun getSettings() {
        // Retrieve settings and check if they exist, otherwise set default values
        val settingsMap = getSharedPreferences(applicationContext, "settings", "settings_key")

        if (settingsMap == null || settingsMap.isEmpty() || !settingsMap.containsKey("sampling rate")) {
            Log.d("MainActivity", "Settings not found, setting default values.")
            val defaultMap = mapOf("sampling rate" to 0.5)
            setSharedPreferences(applicationContext, defaultMap, "settings", "settings_key")
        } else {
            Log.d("MainActivity", "Settings retrieved successfully. Sampling rate: ${settingsMap["sampling rate"]}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MainActivity", "onDestroy called")
    }
}
