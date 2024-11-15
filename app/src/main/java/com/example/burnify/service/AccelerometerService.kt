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
import com.example.burnify.model.AccelerometerMeasurements
import com.example.burnify.model.AccelerometerSample
import com.example.burnify.viewmodel.AccelerometerViewModel

class AccelerometerService : Service(), SensorEventListener {

    // SensorManager to access system sensors
    private val sensorManager: SensorManager by lazy {
        getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    // Accelerometer sensor instance
    private val accelerometer: Sensor? by lazy {
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    // Sampling interval in milliseconds
    private val samplingInterval: Long = 250L

    // Container for accelerometer data
    private val accelerometerData = AccelerometerMeasurements()

    // Handler for periodic updates
    private val handler: Handler by lazy { Handler(Looper.getMainLooper()) }

    // ViewModel to manage accelerometer data
    private lateinit var viewModel: AccelerometerViewModel

    override fun onCreate() {
        super.onCreate()

        // Initialize the ViewModel for data management
        viewModel = ViewModelProvider.AndroidViewModelFactory(application).create(
            AccelerometerViewModel::class.java
        )

        // Register the sensor listener if the accelerometer is available
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }

        // Start periodic data collection
        startDataCollection()
    }

    // Starts periodic data collection, updating every `samplingInterval` milliseconds
    private fun startDataCollection() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                // Update ViewModel with the latest accelerometer data
                viewModel.updateAccelerometerData(accelerometerData)

                // Reschedule the next update
                handler.postDelayed(this, samplingInterval)
            }
        }, samplingInterval)
    }

    // Sends accelerometer data via broadcast
    private fun sendAccelerometerData() {
        val intent = Intent("com.example.burnify.ACCELEROMETER_DATA").apply {
            putExtra("data", accelerometerData) // Include accelerometer data
        }
        sendBroadcast(intent)
    }

    // Called when sensor data changes
    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                // Reuse an existing object to reduce object creation
                val sample = AccelerometerSample().apply {
                    setSample(it.values[0], it.values[1], it.values[2])
                }
                accelerometerData.addSample(sample)

                // Send data via broadcast
                sendAccelerometerData()
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
