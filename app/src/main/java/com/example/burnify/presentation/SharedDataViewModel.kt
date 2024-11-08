package com.example.burnify.presentation


import androidx.lifecycle.ViewModel

class SharedDataViewModel : ViewModel() {
    private val accelerometerMeasurements = AccelerometerMeasurements()
    fun getAccelerometerMeasurements(): AccelerometerMeasurements { return accelerometerMeasurements }

    private val compassMeasurements = CompassMeasurements()
    fun getCompassMeasurements(): CompassMeasurements { return compassMeasurements }
}
