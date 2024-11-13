package com.example.burnify.service

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
import com.example.burnify.model.GyroscopeMeasurements
import com.example.burnify.model.GyroscopeSample
import com.example.burnify.viewmodel.GyroscopeViewModel

class GyroscopeService : Service(), SensorEventListener {

    // SensorManager per accedere ai sensori di sistema
    private lateinit var sensorManager: SensorManager

    // Sensore per il giroscopio
    private var gyroscope: Sensor? = null

    // Intervallo di campionamento in millisecondi
    private val samplingInterval: Long = 250

    // Contenitore per i dati del giroscopio
    private val gyroscopeData = GyroscopeMeasurements()

    // Handler per gestire il post-delay e aggiornare i dati periodicamente
    private val handler = Handler(Looper.getMainLooper())

    // ViewModel per gestire i dati del giroscopio
    private lateinit var viewModel: GyroscopeViewModel

    // Metodo di inizializzazione del servizio
    override fun onCreate() {
        super.onCreate()
        println("Servizio Giroscopio Inizializzato")

        // Inizializza il SensorManager
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // Ottiene il sensore del giroscopio
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        // Inizializza il ViewModel per l'aggiornamento dei dati
        viewModel = ViewModelProvider.AndroidViewModelFactory(application).create(GyroscopeViewModel::class.java)

        // Registra il listener per il giroscopio con un intervallo di 250 microsecondi
        gyroscope?.let {
            sensorManager.registerListener(this, it, 1000 * 250)
        }

        // Avvia la raccolta periodica dei dati
        startDataCollection()
    }

    // Avvia la raccolta periodica dei dati, eseguendo l'aggiornamento ogni "samplingInterval" millisecondi
    private fun startDataCollection() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                // Aggiorna i dati del giroscopio nel ViewModel
                viewModel.updateGyroscopeData(gyroscopeData)

                // Ripianifica l'aggiornamento
                handler.postDelayed(this, samplingInterval)
            }
        }, samplingInterval)
    }

    // Metodo per inviare i dati tramite un broadcast
    private fun sendGyroscopeData() {
        val intent = Intent("com.example.burnify.GYROSCOPE_DATA")
        intent.putExtra("data", gyroscopeData) // Dati del giroscopio da inviare
        sendBroadcast(intent)
    }

    // Metodo richiamato quando i dati del sensore cambiano
    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_GYROSCOPE) {
                val sample = GyroscopeSample()
                sample.setSample(it.values[0], it.values[1], it.values[2])
                gyroscopeData.addSample(sample)

                // Invia i dati tramite broadcast
                sendGyroscopeData()
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
