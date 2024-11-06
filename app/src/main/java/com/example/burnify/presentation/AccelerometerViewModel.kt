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

class AccelerometerViewModel(application: Application) : AndroidViewModel(application) {
    private val _accelerometerData = MutableLiveData<List<AccelerometerSample>>()
    val accelerometerData: LiveData<List<AccelerometerSample>> = _accelerometerData

    private val measurements = AccelerometerMeasurements() // Crea un'istanza di AccelerometerMeasurements

    // Dichiarazione di sensorManager e sensorEventListener come variabili di classe
    private val sensorManager: SensorManager =
        application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            // Aggiorna i dati dell'accelerometro con i nuovi valori
            measurements.addSample(AccelerometerSample(x, y, z))

            _accelerometerData.postValue(measurements.getSamples()) // Passa la lista di campioni

        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }

    init {
        initializeAccelerometerListener()
    }

    private fun initializeAccelerometerListener() {
        val accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        accelerometerSensor?.let {
            // Registra l'ascoltatore con un ritardo di 40 millisecondi (25 Hz)
            sensorManager.registerListener(sensorEventListener, it, 40 * 1000) // 40 ms in microsecondi


        }
    }

    override fun onCleared() {
        super.onCleared()
        // Deregistra il listener quando il ViewModel è distrutto
        sensorManager.unregisterListener(sensorEventListener)
    }
}
