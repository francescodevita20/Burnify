package com.example.burnify.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.burnify.model.MagnetometerMeasurements

class MagnetometerViewModel(application: Application) : AndroidViewModel(application) {

    // LiveData per i dati del magnetometro
    private val _magnetometerData = MutableLiveData<MagnetometerMeasurements>()

    val magnetometerData: LiveData<MagnetometerMeasurements> get() = _magnetometerData

    // Funzione per aggiornare i dati del magnetometro
    fun updateMagnetometerData(magnetometerMeasurements: MagnetometerMeasurements) {
        _magnetometerData.postValue(magnetometerMeasurements)
    }
}
