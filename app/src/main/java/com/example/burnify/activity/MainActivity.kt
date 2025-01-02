package com.example.burnify.activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.burnify.databinding.ActivityMainBinding
import com.example.burnify.service.UnifiedSensorService
import com.example.burnify.ui.screens.OnboardingSettings
import com.example.burnify.util.SensorDataManager
import com.example.burnify.util.setSharedPreferences
import com.example.burnify.util.getSharedPreferences
import com.example.burnify.viewmodel.AccelerometerViewModel
import com.example.burnify.viewmodel.GyroscopeViewModel
import com.example.burnify.viewmodel.LastPredictionViewModel
import com.example.burnify.viewmodel.MagnetometerViewModel
import com.example.burnify.viewmodel.PredictedActivityViewModel
import com.example.burnify.R

/**
 * MainActivity is the entry point of the app where sensor services are initialized and the UI is set.
 */
class MainActivity : AppCompatActivity() {

    // ViewModels for the sensors and predicted activity
    private val accelerometerViewModel: AccelerometerViewModel by viewModels()
    private val gyroscopeViewModel: GyroscopeViewModel by viewModels()
    private val magnetometerViewModel: MagnetometerViewModel by viewModels()
    private val predictedActivityViewModel: PredictedActivityViewModel by viewModels()
    private val lastPredictionViewModel: LastPredictionViewModel by viewModels()

    // ViewBinding instance to bind XML layout
    private lateinit var binding: ActivityMainBinding
    val mainBinding get() = binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize view binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        SensorDataManager.lastPredictionViewModel = lastPredictionViewModel

        // Show battery optimization dialog if necessary
        showBatteryOptimizationDialog()
        // Start sensor services (accelerometer, gyroscope, magnetometer)
        startUnifiedSensorService()
        // navigate from screen to screen
        nav()

        Log.d("MainActivity", "onCreate called")

        // Retrieve user data from SharedPreferences
        val userData = getSharedPreferences("userdata", MODE_PRIVATE)

        // Check if user data is missing
        if (userData == null || !userData.contains("weight") || !userData.contains("height") || !userData.contains("age")) {
            Log.d("MainActivity", "User data missing, navigating to OnboardingSettings fragment")

            // Hide main content and show the OnboardingSettings fragment
            binding.fragmentContainerView6.visibility = View.GONE
            binding.onboardingSettings.visibility = View.VISIBLE

            // Replace the OnboardingSettings fragment into the container
            supportFragmentManager.beginTransaction()
                .replace(R.id.onboardingSettings, OnboardingSettings())  // Ensure correct container ID
                .commit()

            // Return early to avoid further UI setup
            return
        }

    }
    /**
     *switch from onboardingsettings screen to main content
     **/
    fun switchToMainContent() {
        binding.fragmentContainerView6.visibility = View.VISIBLE
        binding.onboardingSettings.visibility = View.GONE
    }

    /**
     *function to set the navigation component using botomNavigation Nav_graph
     */
    private fun nav() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView6) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNavigation.setupWithNavController(navController)
    }
    /**
     * Function to update the UI based on sensor data
     */
    private fun updateUI() {
        // Observe changes in the accelerometer data
        accelerometerViewModel.accelerometerData.observe(this) { data ->
            binding.accelerometerDataTextView.text = "Accelerometer Data: $data"
        }

        // Observe changes in the gyroscope data
        gyroscopeViewModel.gyroscopeData.observe(this) { data ->
            binding.gyroscopeDataTextView.text = "Gyroscope Data: $data"
        }

        // Observe changes in the magnetometer data
        magnetometerViewModel.magnetometerData.observe(this) { data ->
            binding.magnetometerDataTextView.text = "Magnetometer Data: $data"
        }

        // Observe predicted activity data
        predictedActivityViewModel.predictedActivityData.observe(this) { data ->
            binding.predictedActivityTextView.text = "Predicted Activity: $data"
        }

        // Observe last prediction data
        lastPredictionViewModel.lastPredictionData.observe(this) { data ->
            binding.lastPredictionTextView.text = "Last Prediction: $data"
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
     * Starts the unified sensor service in the foreground.
     */
    private fun startUnifiedSensorService() {
        Log.d("MainActivity", "Starting UnifiedSensorService")

        // Retrieve the working mode and pass it to the service
        val workingMode = getSharedPreferences(applicationContext, "settings", "settings_key")?.get("workingmode")

        if (workingMode == null) {
            Log.w("MainActivity", "Working mode is not set. Defaulting to 'maxaccuracy'")
        }

        val unifiedServiceIntent = Intent(this, UnifiedSensorService::class.java).apply {
            putExtra("workingmode", workingMode?.toString() ?: "maxaccuracy") // Default to "maxaccuracy" if null
        }
        try {
            startForegroundService(unifiedServiceIntent)
            Log.d("MainActivity", "UnifiedSensorService started successfully")
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to start UnifiedSensorService", e)
        }
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
