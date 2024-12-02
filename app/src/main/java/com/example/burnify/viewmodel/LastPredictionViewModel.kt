package com.example.burnify.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.burnify.model.AccelerometerMeasurements

class LastPredictionViewModel(application: Application) : AndroidViewModel(application) {



    private val _lastPredictionData = MutableLiveData<Int>() // Singolo campione

    val lastPredictionData : LiveData<Int> get() = _lastPredictionData

    fun updateLastPredictionData(lastPrediction: Int){
        _lastPredictionData.postValue(lastPrediction)
        //println(accelerometerData.value?.getSamples()?.lastOrNull()?.getX())

        println("Last Prediction: $lastPrediction")
    }


}
