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
import com.example.burnify.model.MagnetometerMeasurements
import com.example.burnify.model.MagnetometerSample
import com.example.burnify.viewmodel.MagnetometerViewModel

class MagnetometerService : Service(), SensorEventListener {

    // SensorManager per accedere ai sensori di sistema
    private lateinit var sensorManager: SensorManager

    // Sensore per il magnetometro
    private var magnetometer: Sensor? = null

    // Intervallo di campionamento in millisecondi
    private val samplingInterval: Long = 250

    // Contenitore per i dati del magnetometro
    private val magnetometerData = MagnetometerMeasurements()

    // Handler per gestire il post-delay e aggiornare i dati periodicamente
    private val handler = Handler(Looper.getMainLooper())

    // ViewModel per gestire i dati del magnetometro
    private lateinit var viewModel: MagnetometerViewModel

    override fun onCreate() {
        super.onCreate()
        println("Servizio Magnetometro Inizializzato")

        // Inizializza il SensorManager
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // Ottiene il sensore del magnetometro
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        // Inizializza il ViewModel per l'aggiornamento dei dati
        viewModel = ViewModelProvider.AndroidViewModelFactory(application).create(MagnetometerViewModel::class.java)

        // Registra il listener per il magnetometro con un intervallo di 250 microsecondi
        magnetometer?.let {
            sensorManager.registerListener(this, it, 1000 * 250)
        }

        // Avvia la raccolta periodica dei dati
        startDataCollection()
    }

    private fun startDataCollection() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                // Aggiorna i dati del magnetometro nel ViewModel
                viewModel.updateMagnetometerData(magnetometerData)

                // Ripianifica l'aggiornamento
                handler.postDelayed(this, samplingInterval)
            }
        }, samplingInterval)
    }

    // Metodo per inviare i dati tramite un broadcast
    private fun sendMagnetometerData() {
        val intent = Intent("com.example.burnify.MAGNETOMETER_DATA")
        intent.putExtra("data", magnetometerData) // Dati del magnetometro da inviare
        sendBroadcast(intent)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                val sample = MagnetometerSample()
                sample.setSample(it.values[0], it.values[1], it.values[2])
                magnetometerData.addSample(sample)

                // Invia i dati tramite broadcast
                sendMagnetometerData()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // Deregistra il listener del sensore per risparmiare risorse
        sensorManager.unregisterListener(this)

        // Rimuove tutti i callback e i messaggi dall'handler
        handler.removeCallbacksAndMessages(null)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Nessuna azione necessaria per questo esempio
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
