package com.example.burnify.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class ManagerViewModel(
    application: Application,
    shouldMonitorAccelerometer: Boolean,
    shouldMonitorCompass: Boolean
) : AndroidViewModel(application) {

    private val accelerometerViewModel = AccelerometerViewModel(application)
    private val compassViewModel = CompassViewModel(application)

    private val accelerometerData = MutableLiveData<List<AccelerometerSample>>()
    private val compassData = MutableLiveData<List<CompassSample>>()

    init {
        // Attiva l'ascoltatore dell'accelerometro se il parametro è true
        if (shouldMonitorAccelerometer) {
            accelerometerViewModel.getAccelerometerSamples().observeForever {
                accelerometerData.postValue(it)
            }
        }

        // Attiva l'ascoltatore della bussola se il parametro è true
        if (shouldMonitorCompass) {
            /*compassViewModel.getCompassSamples().observeForever {
                compassData.postValue(it)
            }*/
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Deregistra gli osservatori quando il ViewModel viene distrutto
        accelerometerViewModel.getAccelerometerData().removeObserver { }
        //compassViewModel.compassData.removeObserver { }
    }

    //fun getAccelerometerData(): List<AccelerometerSample> {AccelerometerViewModel.getAccelerometerSamples()}

}
