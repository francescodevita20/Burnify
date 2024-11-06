package com.example.burnify.presentation

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlin.math.abs

class CompassViewModel(application: Application) : AndroidViewModel(application) {
    private val _compassDirection = MutableLiveData<Float>()
    val compassDirection: LiveData<Float> = _compassDirection

    private var lastDirection: Float = 0f // Ultima direzione registrata

    // Definire sensorManager e sensorEventListener come variabili di classe
    private val sensorManager: SensorManager =
        application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                val rotationMatrix = FloatArray(9)
                val orientation = FloatArray(3)

                // Converti il vettore di rotazione in una matrice di rotazione
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                SensorManager.getOrientation(rotationMatrix, orientation)

                // Calcola l'azimuth (direzione della bussola) in gradi
                val azimuthInDegrees = Math.toDegrees(orientation[0].toDouble()).toFloat()
                val normalizedAzimuth = (azimuthInDegrees + 360) % 360 // Normalizza a [0, 360]

                // Aggiorna solo se la differenza rispetto all'ultima direzione è maggiore di 1 grado
                if (abs(normalizedAzimuth - lastDirection) > 1) {
                    lastDirection = normalizedAzimuth
                    _compassDirection.postValue(normalizedAzimuth)
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }

    init {
        initializeCompassListener()
    }

    private fun initializeCompassListener() {
        val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        rotationSensor?.let {
            sensorManager.registerListener(sensorEventListener, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Deregistra il listener quando il ViewModel è distrutto
        sensorManager.unregisterListener(sensorEventListener)
    }
}
