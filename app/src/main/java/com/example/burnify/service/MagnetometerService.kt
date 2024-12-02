package com.example.burnify.service

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
import com.example.burnify.model.MagnetometerMeasurements
import com.example.burnify.model.MagnetometerSample
import com.example.burnify.util.SensorDataManager
import com.example.burnify.viewmodel.MagnetometerViewModel

/**
 * A service that handles magnetometer data collection in the background.
 * It operates as a foreground service to prevent being terminated by the system.
 */
class MagnetometerService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var magnetometer: Sensor? = null

    private var samplingRateInMillis: Long = 1000 // Default value: 1 second
    private var samplesBatch: Int = 100 // Default value: 100 samples per batch

    private val magnetometerData = MagnetometerMeasurements()
    private val handler = Handler(Looper.getMainLooper())
    private val sample = MagnetometerSample()

    private lateinit var viewModel: MagnetometerViewModel

    private var samplesCount = 0

    override fun onCreate() {
        super.onCreate()

        // Log to confirm the service is initialized
        println("Magnetometer Service initialized")

        // Set up the foreground service notification
        startForegroundWithNotification()

        // Initialize the SensorManager and get the magnetometer sensor
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        // Initialize the ViewModel to handle data updates
        viewModel = ViewModelProvider.AndroidViewModelFactory(application).create(MagnetometerViewModel::class.java)

        // Load settings from SharedPreferences to configure working mode
        val sharedPreferences = getSharedPreferences("setting", Context.MODE_PRIVATE)
        val workingMode = sharedPreferences.getString("workingmode", "maxaccuracy") ?: "maxaccuracy"
        setSamplingRateAndBatchSize(workingMode)

        // Log the current configuration
        println("Service started with Sampling Rate: ${samplingRateInMillis}ms, Batch Size: $samplesBatch")

        // Register the magnetometer sensor listener
        magnetometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }

        // Start the data collection process
        startDataCollection()
    }

    /**
     * Set the sampling rate and batch size based on the selected working mode.
     */
    private fun setSamplingRateAndBatchSize(workingMode: String) {
        when (workingMode) {
            "maxbatterysaving" -> {
                samplingRateInMillis = 1000 // 1 second for battery-saving mode
                samplesBatch = 100
            }
            "maxaccuracy" -> {
                samplingRateInMillis = 250 // 250ms for maximum accuracy mode
                samplesBatch = 50
            }
            else -> {
                samplingRateInMillis = 1000 // Default to 1 second
                samplesBatch = 100
            }
        }
    }

    /**
     * Set up the foreground service with a notification.
     */
    private fun startForegroundWithNotification() {
        val channelId = "MagnetometerServiceChannel"
        val channelName = "Magnetometer Service"
        val notificationId = 1003

        // Create a notification channel for Android 8.0 (API 26) and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }

        val notificationHelper = NotificationHelper(this)

        // Create and show the notification for the magnetometer service
        val magnetometerNotification = notificationHelper.createServiceNotification("Magnetometer Service")
        notificationHelper.notify(notificationId, magnetometerNotification)

        // Create and publish a group notification
        val groupNotification = notificationHelper.createGroupNotification()
        notificationHelper.notify(1000, groupNotification)

        // Start the service in the foreground with the notification
        startForeground(notificationId, magnetometerNotification)
    }

    /**
     * Handles the start command, including setting the sampling rate if passed in the intent.
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Set the sampling rate from the intent (if provided)
        samplingRateInMillis = (intent?.getDoubleExtra("samplingRateInSeconds", 1.0)?.times(1000))?.toLong() ?: samplingRateInMillis
        println("Service started with Sampling Rate: ${samplingRateInMillis}ms")
        return START_STICKY
    }

    /**
     * Starts a recurring task to collect magnetometer data at the specified sampling rate.
     */
    private fun startDataCollection() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                // Update the ViewModel with the collected magnetometer data
                viewModel.updateMagnetometerData(magnetometerData)

                // Reschedule the data collection task
                handler.postDelayed(this, samplingRateInMillis)
            }
        }, samplingRateInMillis)
    }

    /**
     * Called when new magnetometer data is available. Processes the data and checks if the batch is full.
     */
    override fun onSensorChanged(event: SensorEvent?) {
        if (SensorDataManager.magnetometerIsFilled) {
            // If the service is paused, wait for other services to complete
            println("Magnetometer service is paused, waiting for other services")
            return
        } else {
            event?.let {
                if (it.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                    // Process the magnetometer data and add it to the measurements
                    sample.setSample(it.values[0], it.values[1], it.values[2])
                    magnetometerData.addSample(applicationContext, sample)

                    samplesCount++
                    if (samplesCount >= samplesBatch) {
                        // Once the batch is full, send the data and reset the counter
                        sendMagnetometerData()
                        samplesCount = 0
                    }
                }
            }
        }
    }

    /**
     * Sends the collected magnetometer data via a broadcast intent.
     */
    private fun sendMagnetometerData() {
        val intent = Intent("com.example.burnify.MAGNETOMETER_DATA")
        intent.putExtra("data", magnetometerData)
        sendBroadcast(intent)
    }

    /**
     * Cleans up resources when the service is destroyed.
     */
    override fun onDestroy() {
        super.onDestroy()
        // Unregister the sensor listener and remove any pending tasks
        sensorManager.unregisterListener(this)
        handler.removeCallbacksAndMessages(null)
        println("Magnetometer Service stopped")
    }

    /**
     * Called when the sensor accuracy changes (not used in this case).
     */
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    /**
     * Binds to the service (not used in this case).
     */
    override fun onBind(intent: Intent?): IBinder? = null
}
