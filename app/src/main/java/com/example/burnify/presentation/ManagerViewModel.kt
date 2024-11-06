package com.example.burnify.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class ManagerViewModel(application: Application) : AndroidViewModel(application) {

    private val accelerometerViewModel = AccelerometerViewModel(application)
    private val compassViewModel = CompassViewModel(application)

    private val _accelerometerData = MutableLiveData<List<AccelerometerSample>>()
    val accelerometerData: LiveData<List<AccelerometerSample>> = _accelerometerData

    private val _compassData = MutableLiveData<List<CompassSample>>()
    val compassData: LiveData<List<CompassSample>> = _compassData

    init {
        // Osserva i dati accelerometro
        accelerometerViewModel.accelerometerData.observeForever {
            _accelerometerData.postValue(it)
        }

        // Osserva i dati bussola
        compassViewModel.compassData.observeForever {
            _compassData.postValue(it)
        }
    }

    // Funzione per forzare l'aggiornamento della bussola se è necessario
    fun checkAndUpdateCompassData() {
        val compassSamples = compassViewModel.getCompassSamples()

        // Controlla se ci sono campioni della bussola
        if (compassSamples.isNotEmpty()) {
            val currentDirection = compassSamples.last().angle  // Usa 'angle' al posto di 'azimuth'
            val lastDirection = compassSamples.first().angle   // Usa 'angle' al posto di 'azimuth'

            // Controlla se l'angolo della bussola è cambiato di più di 1 grado
            if (Math.abs(currentDirection - lastDirection) > 1) {
                // Aggiungi un nuovo campione della bussola
                _compassData.postValue(compassSamples)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Deregistra gli osservatori quando il ViewModel viene distrutto
        accelerometerViewModel.accelerometerData.removeObserver { }
        compassViewModel.compassData.removeObserver { }
    }
}
