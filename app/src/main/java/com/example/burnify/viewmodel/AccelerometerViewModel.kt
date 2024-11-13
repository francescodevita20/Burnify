package com.example.burnify.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.burnify.model.AccelerometerMeasurements

class AccelerometerViewModel(application: Application) : AndroidViewModel(application) {



    private val _accelerometerData = MutableLiveData<AccelerometerMeasurements>() // Singolo campione

    val accelerometerData : LiveData<AccelerometerMeasurements> get() = _accelerometerData


    fun updateAccelerometerData(accelerometerMeasurements: AccelerometerMeasurements){
        _accelerometerData.postValue(accelerometerMeasurements)
        //println(accelerometerData.value?.getSamples()?.lastOrNull()?.getX())
    }

}
