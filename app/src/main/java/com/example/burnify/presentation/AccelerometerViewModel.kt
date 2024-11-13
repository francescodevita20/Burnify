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



    private val _accelerometerData = MutableLiveData<AccelerometerMeasurements>() // Singolo campione

    val accelerometerData : LiveData<AccelerometerMeasurements> get() = _accelerometerData


    fun updateAccelerometerData(accelerometerMeasurements: AccelerometerMeasurements){
        _accelerometerData.postValue(accelerometerMeasurements)
        //println(accelerometerData.value?.getSamples()?.lastOrNull()?.getX())
    }





/*
private val sensorManager: SensorManager =

   application.getSystemService(Context.SENSOR_SERVICE) as SensorManager


privaval sensorEventListener = object : SensorEventListener {
   override fun onSensorChanged(event: SensorEvent) {
       // Estrai i valori direttamente
       val (x, y, z) = event.values

       // Crea e aggiorna il campione
       val sample = AccelerometerSample()
       sample.setSample(x, y, z)

       // Posta il campione direttamente
       accelerometerData.postValue(sample)
   }

   override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
}

init {
   initializeAccelerometerListener()
}

private fun initializeAccelerometerListener() {
   val accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
   accelerometerSensor?.let {
       sensorManager.registerListener(sensorEventListener, it, 40 * 1000) // 40ms
   }
}

override fun onCleared() {
   super.onCleared()
   sensorManager.unregisterListener(sensorEventListener)
}

fun getAccelerometerData(): LiveData<AccelerometerSample> = accelerometerData
*/

}
