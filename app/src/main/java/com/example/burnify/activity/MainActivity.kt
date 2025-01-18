package com.example.burnify.activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.burnify.databinding.ActivityMainBinding
import com.example.burnify.service.UnifiedSensorService
import com.example.burnify.ui.screens.OnboardingSettings
import com.example.burnify.util.SensorDataManager
import com.example.burnify.viewmodel.LastPredictionViewModel
import com.example.burnify.R
import android.graphics.Color


/**
 * MainActivity is the entry point of the app where sensor services are initialized and the UI is set.
 */
class MainActivity : AppCompatActivity() {

    private val lastPredictionViewModel: LastPredictionViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize view binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //setup the toolbar
        setSupportActionBar(binding.toolbar)

        SensorDataManager.lastPredictionViewModel = lastPredictionViewModel

        // Check user data before initializing other components
        if (checkUserDataAndNavigate()) {
            showBatteryOptimizationDialog()
            initializeAppComponents()
        }
        setupToolbarTitleBasedOnFragment()
    }
    private fun checkUserDataAndNavigate(): Boolean {
        val sharedPrefs = getSharedPreferences("userdata", MODE_PRIVATE)

        val hasAllData = sharedPrefs.contains("weight") &&
                sharedPrefs.contains("height") &&
                sharedPrefs.contains("age") &&
                sharedPrefs.contains("gender")

        return if (!hasAllData) {
            Log.d("MainActivity", "User data missing, showing OnboardingSettings fragment")

            // Hide navigation and main content
            binding.bottomNavigation.visibility = View.GONE
            binding.fragmentContainerView6.visibility = View.GONE
            binding.onboardingSettings.visibility = View.VISIBLE

            // Load the OnboardingSettings fragment
            supportFragmentManager.beginTransaction()
                .replace(R.id.onboardingSettings, OnboardingSettings())
                .commit()

            false
        } else {
            // Show main content
            binding.bottomNavigation.visibility = View.VISIBLE
            binding.fragmentContainerView6.visibility = View.VISIBLE
            binding.onboardingSettings.visibility = View.GONE
            true
        }
    }

    private fun initializeAppComponents() {
        startUnifiedSensorService()
        nav()
    }
    /**
     *switch from onboardingsettings screen to main content
     **/
    fun switchToMainContent() {
        // Show main content
        binding.bottomNavigation.visibility = View.VISIBLE
        binding.fragmentContainerView6.visibility = View.VISIBLE
        binding.onboardingSettings.visibility = View.GONE

        // Initialize app components
        initializeAppComponents()
    }
    /**
     *function to set the navigation component using botomNavigation Nav_graph
     */
    private fun nav() {
        try {
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView6) as NavHostFragment
            val navController = navHostFragment.navController
            binding.bottomNavigation.setupWithNavController(navController)
        } catch (e: Exception) {
            Log.e("MainActivity", "Error setting up navigation", e)
        }
    }
    /**
     * Shows a dialog asking the user to disable battery optimization if it's not already disabled.
     */
    private fun showBatteryOptimizationDialog() {
        // Get the power manager to check battery optimization settings
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        val isIgnoringBatteryOptimizations =
            powerManager.isIgnoringBatteryOptimizations(packageName)

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
     * Starts the unified sensor service in the foreground without checking the working mode.
     */
    private fun startUnifiedSensorService() {
        Log.d("MainActivity", "Starting UnifiedSensorService")

        val unifiedServiceIntent = Intent(this, UnifiedSensorService::class.java)
        try {
            startForegroundService(unifiedServiceIntent)
            Log.d("MainActivity", "UnifiedSensorService started successfully")
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to start UnifiedSensorService", e)
        }
    }

    /**
     * Stops the UnifiedSensorService.
     */
    fun stopUnifiedSensorService() {
        // Make sure to stop the service when exiting
        val unifiedServiceIntent = Intent(this, UnifiedSensorService::class.java)
        stopService(unifiedServiceIntent)
        Log.d("MainActivity", "UnifiedSensorService stopped successfully.")
    }

    private fun setupToolbarTitleBasedOnFragment() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView6) as? NavHostFragment
        val navController = navHostFragment?.navController

        if (navController != null) {
            navController.addOnDestinationChangedListener { _, destination, _ ->
                val fragmentLabel = destination.label.toString()

                when (fragmentLabel) {
                    "today_screen" -> {
                        setToolbarTitle("Today's Screen")
                        supportActionBar?.setDisplayHomeAsUpEnabled(false)
                        invalidateOptionsMenu()  // This will trigger the menu to be recreated (including the Exit button)
                    }
                    "settings_screen" -> {
                        setToolbarTitle("Settings")
                        supportActionBar?.setDisplayHomeAsUpEnabled(true)
                        invalidateOptionsMenu()  // Ensure the Exit button is hidden when on other screens
                    }
                    "data_screen" -> {
                        setToolbarTitle("Data Screen")
                        supportActionBar?.setDisplayHomeAsUpEnabled(true)
                        invalidateOptionsMenu()  // Ensure the Exit button is hidden when on other screens
                    }
                    else -> {
                        setToolbarTitle("Burnify")
                        supportActionBar?.setDisplayHomeAsUpEnabled(false)
                        invalidateOptionsMenu()  // Ensure the Exit button is hidden when on other screens
                    }
                }
            }
        } else {
            Log.e("MainActivity", "NavController is null, unable to set toolbar title")
        }
    }

    private fun setToolbarTitle(title: String) {
        supportActionBar?.title = title
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_exit -> {
                item.icon?.setTintList(ColorStateList.valueOf(Color.RED))
                // Stop the UnifiedSensorService before exiting the app
                stopUnifiedSensorService()
                finish()  // This will close the app or activity when "Exit" is clicked
                true
            }
            android.R.id.home -> {
                onBackPressed()  // This triggers back navigation
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Get the current fragment displayed in the NavHostFragment
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView6) as? NavHostFragment
        val navController = navHostFragment?.navController
        val currentDestination = navController?.currentDestination

        // Check if the current fragment is the "Today Screen"
        if (currentDestination != null && currentDestination.label == "settings_screen") {
            // Inflate the menu only if we are on the "Today" screen
            menuInflater.inflate(R.menu.toolbar_menu, menu)
        }

        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MainActivity", "onDestroy called")
    }

}