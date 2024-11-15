package com.example.burnify.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.lifecycle.ViewModelProvider
import com.example.burnify.model.MagnetometerMeasurements
import com.example.burnify.model.MagnetometerSample
import com.example.burnify.viewmodel.MagnetometerViewModel

class MagnetometerService : Service(), SensorEventListener {

    // SensorManager to access system sensors
    private val sensorManager: SensorManager by lazy {
        getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    // Magnetometer sensor instance
    private val magnetometer: Sensor? by lazy {
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    }

    // Sampling interval in milliseconds
    private val samplingInterval: Long = 250L

    // Container for magnetometer data
    private val magnetometerData = MagnetometerMeasurements()

    // Handler for periodic updates
    private val handler: Handler by lazy { Handler(Looper.getMainLooper()) }

    // ViewModel to manage magnetometer data
    private lateinit var viewModel: MagnetometerViewModel

    override fun onCreate() {
        super.onCreate()

        // Initialize the ViewModel
        viewModel = ViewModelProvider.AndroidViewModelFactory(application).create(
            MagnetometerViewModel::class.java
        )

        // Register the magnetometer listener if available
        magnetometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }

        // Start periodic data collection
        startDataCollection()
    }

    // Start periodic data collection
    private fun startDataCollection() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                // Update the ViewModel with magnetometer data
                viewModel.updateMagnetometerData(magnetometerData)

                // Schedule the next update
                handler.postDelayed(this, samplingInterval)
            }
        }, samplingInterval)
    }

    // Send magnetometer data via broadcast
    private fun sendMagnetometerData() {
        val intent = Intent("com.example.burnify.MAGNETOMETER_DATA").apply {
            putExtra("data", magnetometerData) // Include magnetometer data
        }
        sendBroadcast(intent)
    }

    // Called when sensor data changes
    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                // Reuse an existing sample to reduce object creation
                val sample = MagnetometerSample().apply {
                    setSample(it.values[0], it.values[1], it.values[2])
                }
                magnetometerData.addSample(sample)

                // Send the data via broadcast
                sendMagnetometerData()
            }
        }
    }

    // Called when the service is destroyed
    override fun onDestroy() {
        super.onDestroy()

        // Unregister the sensor listener to free resources
        sensorManager.unregisterListener(this)

        // Remove all callbacks and messages from the handler
        handler.removeCallbacksAndMessages(null)
    }

    // Not used in this example
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No action needed
    }

    // Not used in this example
    override fun onBind(intent: Intent?): IBinder? = null
}
