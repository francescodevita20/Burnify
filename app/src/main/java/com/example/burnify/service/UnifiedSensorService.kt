package com.example.burnify.service

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.burnify.util.SensorDataManager
import com.example.burnify.util.NotificationHelper.Companion.CHANNEL_ID

class UnifiedSensorService : Service(), SensorEventListener {
    private lateinit var sensorManager: SensorManager

    // Sensor references
    private var accelerometerSensor: Sensor? = null
    private var gyroscopeSensor: Sensor? = null
    private var magnetometerSensor: Sensor? = null

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        initializeSensors()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Create a simple notification for the foreground service
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Sensor Data Collection")
            .setContentText("Collecting data from sensors...")
            .setSmallIcon(android.R.drawable.ic_notification_overlay) // Use a custom icon here
            .build()

        // Start the service as a foreground service
        startForeground(1, notification)

        // Return START_STICKY to ensure the service is restarted if it's killed by the system
        return START_STICKY
    }

    /**
     * Initializes the sensors and registers the listener.
     */
    private fun initializeSensors() {
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        magnetometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        if (accelerometerSensor == null) {
            Log.e("SensorService", "Accelerometer not available")
        } else {
            Log.d("SensorService", "Accelerometer available")
        }

        if (gyroscopeSensor == null) {
            Log.e("SensorService", "Gyroscope not available")
        } else {
            Log.d("SensorService", "Gyroscope available")
        }

        if (magnetometerSensor == null) {
            Log.e("SensorService", "Magnetometer not available")
        } else {
            Log.d("SensorService", "Magnetometer available")
        }

        accelerometerSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        gyroscopeSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        magnetometerSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    /**
     * Unregisters the sensor listener and stops all sensor activity.
     */
    private fun stopSensors() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            Log.d("SensorData", "Sensor type: ${it.sensor.type}, Values: ${it.values.joinToString()}")
            when (it.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> handleAccelerometerData(it)
                Sensor.TYPE_GYROSCOPE -> handleGyroscopeData(it)
                Sensor.TYPE_MAGNETIC_FIELD -> handleMagnetometerData(it)
            }
        }
    }

    private fun handleAccelerometerData(event: SensorEvent) {
        val sample = listOf(event.values[0], event.values[1], event.values[2])
        SensorDataManager.updateAccelerometerData(sample, applicationContext)
    }

    private fun handleGyroscopeData(event: SensorEvent) {
        val sample = listOf(event.values[0], event.values[1], event.values[2])
        SensorDataManager.updateGyroscopeData(sample, applicationContext)
    }

    private fun handleMagnetometerData(event: SensorEvent) {
        val sample = listOf(event.values[0], event.values[1], event.values[2])
        SensorDataManager.updateMagnetometerData(sample, applicationContext)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Handle sensor accuracy changes if needed
    }

    override fun onDestroy() {
        // Clean up resources when the service is stopped
        stopSensors()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        // Return null since this is not a bound service
        return null
    }
}
