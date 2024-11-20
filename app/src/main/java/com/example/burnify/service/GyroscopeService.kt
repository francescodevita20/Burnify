package com.example.burnify.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.lifecycle.ViewModelProvider
import com.example.burnify.NotificationHelper
import com.example.burnify.model.GyroscopeMeasurements
import com.example.burnify.model.GyroscopeSample
import com.example.burnify.viewmodel.GyroscopeViewModel

class GyroscopeService : Service(), SensorEventListener {

    // SensorManager to access system sensors
    private lateinit var sensorManager: SensorManager
    private var gyroscope: Sensor? = null

    // Sampling rate and batch size
    private var samplingRateInMillis: Long = 1000
    private var samplesBatch: Int = 64

    // Container for gyroscope data
    private val gyroscopeData = GyroscopeMeasurements()

    // Handler for periodic updates
    private val handler = Handler(Looper.getMainLooper())
    private val sample = GyroscopeSample()

    // ViewModel to manage gyroscope data
    private lateinit var viewModel: GyroscopeViewModel

    override fun onCreate() {
        super.onCreate()

        // Set up the foreground notification for the service
        startForegroundWithNotification()

        // Initialize the SensorManager and gyroscope sensor
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        // Initialize the ViewModel
        viewModel = ViewModelProvider.AndroidViewModelFactory(application).create(
            GyroscopeViewModel::class.java
        )

        // Read "workingmode" from SharedPreferences
        val sharedPreferences = getSharedPreferences("setting", Context.MODE_PRIVATE)
        val workingMode = sharedPreferences.getString("workingmode", "maxaccuracy") ?: "maxaccuracy"

        // Set sampling rate and batch size based on "workingmode"
        when (workingMode) {
            "maxbatterysaving" -> {
                samplingRateInMillis = 1000 // For example, 5 seconds
                samplesBatch = 64 // Larger batch size to save battery
            }
            "maxaccuracy" -> {
                samplingRateInMillis = 250 // For example, 500 ms for maximum accuracy
                samplesBatch = 32 // Smaller batch size for higher accuracy
            }
            else -> {
                samplingRateInMillis = 1000 // Default value of 1 second
                samplesBatch = 64 // Default batch size
            }
        }

        println("Servizio avviato con Sampling Rate: ${samplingRateInMillis}ms, Batch Size: $samplesBatch")

        // Register the gyroscope listener if available
        gyroscope?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }

        // Start periodic data collection
        startDataCollection()
    }

    private fun startForegroundWithNotification() {
        // Create a notification channel for Android 8.0 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "GyroscopeServiceChannel",
                "Gyroscope Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }

        // Create the notification
        val notificationHelper = NotificationHelper(this)
        val notification = notificationHelper.createServiceNotification("Gyroscope Service")
        startForeground(1003, notification)

        // Update or create the main notification
        val groupNotification = notificationHelper.createGroupNotification()
        notificationHelper.notify(1000, groupNotification)
    }

    private fun startDataCollection() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                // Update the ViewModel with gyroscope data
                viewModel.updateGyroscopeData(gyroscopeData)

                // Reschedule the update
                handler.postDelayed(this, samplingRateInMillis)
            }
        }, samplingRateInMillis)
    }

    private var samplesCount = 0

    // Called when sensor data changes
    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_GYROSCOPE) {
                // Reuse an existing sample to reduce object creation
                sample.setSample(it.values[0], it.values[1], it.values[2])

                gyroscopeData.addSample(applicationContext, sample)

                samplesCount++

                // Send data via broadcast when batch is collected
                if (samplesCount >= samplesBatch) {
                    sendGyroscopeData()
                    samplesCount = 0
                }
            }
        }
    }

    // Send gyroscope data via broadcast
    private fun sendGyroscopeData() {
        val intent = Intent("com.example.burnify.GYROSCOPE_DATA").apply {
            putExtra("data", gyroscopeData) // Include gyroscope data
        }
        sendBroadcast(intent)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Get the sampling rate from the Intent
        samplingRateInMillis = (intent?.getDoubleExtra("samplingRateInSeconds", 1.0)?.times(1000))?.toLong() ?: 1000

        println("Servizio avviato con Sampling Rate: ${samplingRateInMillis}ms")

        // Start the data collection
        startDataCollection()

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        // Unregister the sensor listener to free resources
        sensorManager.unregisterListener(this)

        // Remove all callbacks and messages from the handler
        handler.removeCallbacksAndMessages(null)

        println("Gyroscope Service terminated")
    }

    // Not used in this example
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No action needed
    }

    // Not used in this example
    override fun onBind(intent: Intent?): IBinder? = null
}
