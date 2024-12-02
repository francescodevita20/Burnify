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
import com.example.burnify.model.GyroscopeMeasurements
import com.example.burnify.model.GyroscopeSample
import com.example.burnify.util.SensorDataManager
import com.example.burnify.viewmodel.GyroscopeViewModel

/**
 * A service that manages gyroscope data collection in the background.
 * The service operates in the foreground with a notification to prevent being killed by the system.
 */
class GyroscopeService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var gyroscope: Sensor? = null

    private var samplingRateInMillis: Long = 1000 // Default: 1 second sampling rate
    private var samplesBatch: Int = 100 // Default: 100 samples per batch

    private val gyroscopeData = GyroscopeMeasurements()
    private val handler = Handler(Looper.getMainLooper())
    private val sample = GyroscopeSample()

    private lateinit var viewModel: GyroscopeViewModel

    private var samplesCount = 0

    override fun onCreate() {
        super.onCreate()

        // Log to confirm the service is initialized
        println("Gyroscope Service initialized")

        // Set up the foreground service notification
        startForegroundWithNotification()

        // Initialize the SensorManager and get the gyroscope sensor
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        // Initialize the ViewModel to handle data updates
        viewModel = ViewModelProvider.AndroidViewModelFactory(application).create(GyroscopeViewModel::class.java)

        // Load settings from SharedPreferences to configure working mode
        val sharedPreferences = getSharedPreferences("setting", Context.MODE_PRIVATE)
        val workingMode = sharedPreferences.getString("workingmode", "maxaccuracy") ?: "maxaccuracy"
        setSamplingRateAndBatchSize(workingMode)

        // Log the current configuration
        println("Service started with Sampling Rate: ${samplingRateInMillis}ms, Batch Size: $samplesBatch")

        // Register the gyroscope sensor listener
        gyroscope?.let {
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
        val channelId = "GyroscopeServiceChannel"
        val channelName = "Gyroscope Service"
        val notificationId = 1002

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

        // Create and show the notification for the gyroscope service
        val gyroscopeNotification = notificationHelper.createServiceNotification("Gyroscope Service")
        notificationHelper.notify(notificationId, gyroscopeNotification)

        // Create and publish a group notification
        val groupNotification = notificationHelper.createGroupNotification()
        notificationHelper.notify(1000, groupNotification)

        // Start the service in the foreground with the notification
        startForeground(notificationId, gyroscopeNotification)
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
     * Starts a recurring task to collect gyroscope data at the specified sampling rate.
     */
    private fun startDataCollection() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                // Update the ViewModel with the collected gyroscope data
                viewModel.updateGyroscopeData(gyroscopeData)

                // Reschedule the data collection task
                handler.postDelayed(this, samplingRateInMillis)
            }
        }, samplingRateInMillis)
    }

    /**
     * Called when new gyroscope data is available. Processes the data and checks if the batch is full.
     */
    override fun onSensorChanged(event: SensorEvent?) {
        if (SensorDataManager.gyroscopeIsFilled) {
            // If the service is paused, wait for other services to complete
            println("Gyroscope service is paused, waiting for other services")
            return
        } else {
            event?.let {
                if (it.sensor.type == Sensor.TYPE_GYROSCOPE) {
                    // Process the gyroscope data and add it to the measurements
                    sample.setSample(it.values[0], it.values[1], it.values[2])
                    gyroscopeData.addSample(applicationContext, sample)

                    samplesCount++
                    if (samplesCount >= samplesBatch) {
                        // Once the batch is full, send the data and reset the counter
                        sendGyroscopeData()
                        samplesCount = 0
                    }
                }
            }
        }
    }

    /**
     * Sends the collected gyroscope data via a broadcast intent.
     */
    private fun sendGyroscopeData() {
        val intent = Intent("com.example.burnify.GYROSCOPE_DATA")
        intent.putExtra("data", gyroscopeData)
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
        println("Gyroscope Service stopped")
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
