package com.example.burnify.service

import com.example.burnify.util.SensorDataManager
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
import com.example.burnify.util.NotificationHelper
import com.example.burnify.model.AccelerometerMeasurements
import com.example.burnify.model.AccelerometerSample
import com.example.burnify.viewmodel.AccelerometerViewModel

/**
 * Service that manages accelerometer data collection in the background.
 * It uses a foreground service to keep the service alive and collects sensor data at a specified rate.
 */
class AccelerometerService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    private var samplingRateInMillis: Long = 1000 // Default sampling rate: 1 second
    private var samplesBatch: Int = 100 // Default batch size: 100 samples per batch

    private val accelerometerData = AccelerometerMeasurements()
    private val handler = Handler(Looper.getMainLooper())
    private val sample = AccelerometerSample()

    private lateinit var viewModel: AccelerometerViewModel

    private var samplesCount = 0

    override fun onCreate() {
        super.onCreate()

        // Log to confirm the service has started
        println("Accelerometer Service initialized")

        // Configure notification for Foreground Service
        startForegroundWithNotification()

        // Initialize the SensorManager to manage sensors
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        if (accelerometer == null) {
            println("Error: No Accelerometer found on device")
            stopSelf() // Stop service if no accelerometer is available
            return
        }

        // Initialize the ViewModel to manage data updates
        viewModel = ViewModelProvider.AndroidViewModelFactory(application).create(AccelerometerViewModel::class.java)

        // Load settings from SharedPreferences to configure the working mode
        val sharedPreferences = getSharedPreferences("setting", Context.MODE_PRIVATE)
        val workingMode = sharedPreferences.getString("workingmode", "maxaccuracy") ?: "maxaccuracy"
        setSamplingRateAndBatchSize(workingMode)

        // Log the current settings
        println("Service started with Sampling Rate: ${samplingRateInMillis}ms, Batch Size: $samplesBatch")

        // Register the sensor listener for the accelerometer
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }

        // Start collecting data
        startDataCollection()
    }

    /**
     * Configures the sampling rate and batch size based on the selected working mode.
     */
    private fun setSamplingRateAndBatchSize(workingMode: String) {
        when (workingMode) {
            "maxbatterysaving" -> {
                samplingRateInMillis = 1000 // 1 second for battery saving mode
                samplesBatch = 100
            }
            "maxaccuracy" -> {
                samplingRateInMillis = 250 // 250ms for maximum accuracy
                samplesBatch = 50
            }
            else -> {
                samplingRateInMillis = 1000 // Default to 1 second if mode is unknown
                samplesBatch = 100
            }
        }
    }

    /**
     * Starts the foreground service with a notification.
     */
    private fun startForegroundWithNotification() {
        val channelId = "AccelerometerServiceChannel"
        val channelName = "Accelerometer Service"
        val notificationId = 1001

        // Create a notification channel for API 26 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }

        // Create and display the notification for the service
        val notificationHelper = NotificationHelper(this)
        val accelerometerNotification = notificationHelper.createServiceNotification("Accelerometer Service")
        notificationHelper.notify(notificationId, accelerometerNotification)

        // Start the service in the foreground with the notification
        startForeground(notificationId, accelerometerNotification)
    }

    /**
     * Handles the start command, including setting the sampling rate if passed in the intent.
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        samplingRateInMillis = (intent?.getDoubleExtra("samplingRateInSeconds", 1.0)?.times(1000))?.toLong() ?: samplingRateInMillis
        println("Service started with Sampling Rate: ${samplingRateInMillis}ms")
        return START_STICKY
    }

    /**
     * Starts a recurring task to collect accelerometer data at the specified sampling rate.
     */
    private fun startDataCollection() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                // Check if the accelerometer service is paused before updating data
                if (SensorDataManager.accelerometerIsFilled) {
                    println("Accelerometer service is paused, waiting for other services")
                    return
                }

                // Update the accelerometer data in the ViewModel
                viewModel.updateAccelerometerData(accelerometerData)

                // Reschedule the data collection
                handler.postDelayed(this, samplingRateInMillis)
            }
        }, samplingRateInMillis)
    }

    /**
     * Called when new accelerometer data is available. Processes the data and checks if the batch is full.
     */
    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                sample.setSample(it.values[0], it.values[1], it.values[2])
                accelerometerData.addSample(applicationContext, sample)

                samplesCount++
                if (samplesCount >= samplesBatch) {
                    // Send the data when the batch is full, then reset the counter
                    sendAccelerometerData()
                    samplesCount = 0
                }
            }
        }
    }

    /**
     * Sends the collected accelerometer data via broadcast.
     */
    private fun sendAccelerometerData() {
        val intent = Intent("com.example.burnify.ACCELEROMETER_DATA")
        intent.putExtra("data", accelerometerData)
        sendBroadcast(intent)
    }

    /**
     * Cleans up resources when the service is destroyed.
     */
    override fun onDestroy() {
        super.onDestroy()
        // Unregister the sensor listener and remove scheduled tasks
        sensorManager.unregisterListener(this)
        handler.removeCallbacksAndMessages(null)
        println("Accelerometer Service stopped")
    }

    /**
     * Handles changes in sensor accuracy (not used in this example).
     */
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not required for this service
    }

    /**
     * Binds to the service (not used in this case).
     */
    override fun onBind(intent: Intent?): IBinder? = null
}
