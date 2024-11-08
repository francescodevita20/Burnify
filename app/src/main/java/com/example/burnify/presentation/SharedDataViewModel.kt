package com.example.burnify.presentation


import androidx.lifecycle.ViewModel

class SharedDataViewModel : ViewModel() {
    val accelerometerMeasurements = AccelerometerMeasurements()
    val compassMeasurements = CompassMeasurements()
}
