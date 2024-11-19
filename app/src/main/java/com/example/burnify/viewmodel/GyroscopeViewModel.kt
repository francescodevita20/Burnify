package com.example.burnify.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.burnify.model.GyroscopeMeasurements

class GyroscopeViewModel(application: Application) : AndroidViewModel(application) {

    // LiveData per i dati del giroscopio
    private val _gyroscopeData = MutableLiveData<GyroscopeMeasurements>()
    val gyroscopeData: LiveData<GyroscopeMeasurements> get() = _gyroscopeData

    // Funzione per aggiornare i dati del giroscopio
    fun updateGyroscopeData(gyroscopeMeasurements: GyroscopeMeasurements) {
        _gyroscopeData.postValue(gyroscopeMeasurements)
    }
}
