package com.example.burnify.presentation

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.lifecycle.ViewModelProvider


class AccelerometerService : Service(), SensorEventListener {

    // SensorManager per accedere ai sensori di sistema
    private lateinit var sensorManager: SensorManager

    // Sensore per l'accelerometro
    private var accelerometer: Sensor? = null

    // Intervallo di campionamento in millisecondi
    private val samplingInterval: Long = 250

    // Contenitore per i dati dell'accelerometro
    private val accelerometerData = AccelerometerMeasurements()

    // Handler per gestire il post-delay e aggiornare i dati periodicamente
    private val handler = Handler(Looper.getMainLooper())

    // ViewModel per gestire i dati dell'accelerometro
    private lateinit var viewModel: AccelerometerViewModel

    // Metodo di inizializzazione del servizio
    override fun onCreate() {
        super.onCreate()

        // Inizializza il SensorManager
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // Ottiene il sensore dell'accelerometro
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        // Inizializza il ViewModel per l'aggiornamento dei dati
        viewModel = ViewModelProvider.AndroidViewModelFactory(application).create(AccelerometerViewModel::class.java)

        // Registra il listener per l'accelerometro con un intervallo di 250 microsecondi
        accelerometer?.let {
            sensorManager.registerListener(this, it, 1000*250)
        }

        // Avvia la raccolta periodica dei dati
        startDataCollection()
    }

    // Avvia la raccolta periodica dei dati, eseguendo l'aggiornamento ogni "samplingInterval" millisecondi
    private fun startDataCollection() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                // Aggiorna i dati dell'accelerometro nel ViewModel
                viewModel.updateAccelerometerData(accelerometerData)

                // Ripianifica l'aggiornamento
                handler.postDelayed(this, samplingInterval)
            }
        }, samplingInterval)
    }

    // Metodo per inviare i dati tramite un broadcast
    private fun sendAccelerometerData() {
        val intent = Intent("com.example.burnify.ACCELEROMETER_DATA")
        intent.putExtra("data", accelerometerData) // Dati dell'accelerometro da inviare
        sendBroadcast(intent)
    }

    // Metodo richiamato quando i dati del sensore cambiano
    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                val sample = AccelerometerSample()
                sample.setSample(it.values[0], it.values[1], it.values[2])
                accelerometerData.addSample(sample)



                // Invia i dati tramite broadcast
                sendAccelerometerData()
            }
        }
    }


    // Metodo richiamato quando il servizio viene distrutto
    override fun onDestroy() {
        super.onDestroy()

        // Deregistra il listener del sensore per risparmiare risorse
        sensorManager.unregisterListener(this)

        // Rimuove tutti i callback e i messaggi dall'handler
        handler.removeCallbacksAndMessages(null)
    }

    // Metodo richiamato quando cambia la precisione del sensore (non utilizzato in questo esempio)
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Nessuna azione necessaria per questo esempio
    }

    // Metodo per il binding del servizio (non utilizzato, quindi restituisce null)
    override fun onBind(intent: Intent?): IBinder? = null



}
