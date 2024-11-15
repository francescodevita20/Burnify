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
import com.example.burnify.model.GyroscopeMeasurements
import com.example.burnify.model.GyroscopeSample
import com.example.burnify.viewmodel.GyroscopeViewModel

class GyroscopeService : Service(), SensorEventListener {

    // SensorManager to access system sensors
    private val sensorManager: SensorManager by lazy {
        getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    // Gyroscope sensor instance
    private val gyroscope: Sensor? by lazy {
        sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    }

    // Sampling interval in milliseconds
    private val samplingInterval: Long = 250L

    // Container for gyroscope data
    private val gyroscopeData = GyroscopeMeasurements()

    // Handler for periodic updates
    private val handler: Handler by lazy { Handler(Looper.getMainLooper()) }

    // ViewModel to manage gyroscope data
    private lateinit var viewModel: GyroscopeViewModel

    override fun onCreate() {
        super.onCreate()

        // Initialize the ViewModel
        viewModel = ViewModelProvider.AndroidViewModelFactory(application).create(
            GyroscopeViewModel::class.java
        )

        // Register the gyroscope listener if available
        gyroscope?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }

        // Start periodic data collection
        startDataCollection()
    }

    // Start periodic data collection and update the ViewModel every "samplingInterval" milliseconds
    private fun startDataCollection() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                // Update the ViewModel with gyroscope data
                viewModel.updateGyroscopeData(gyroscopeData)

                // Schedule the next update
                handler.postDelayed(this, samplingInterval)
            }
        }, samplingInterval)
    }

    // Send gyroscope data via broadcast
    private fun sendGyroscopeData() {
        val intent = Intent("com.example.burnify.GYROSCOPE_DATA").apply {
            putExtra("data", gyroscopeData) // Include gyroscope data
        }
        sendBroadcast(intent)
    }

    // Called when sensor data changes
    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_GYROSCOPE) {
                // Reuse an existing sample to reduce object creation
                val sample = GyroscopeSample().apply {
                    setSample(it.values[0], it.values[1], it.values[2])
                }
                gyroscopeData.addSample(sample)

                // Send the data via broadcast
                sendGyroscopeData()
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
