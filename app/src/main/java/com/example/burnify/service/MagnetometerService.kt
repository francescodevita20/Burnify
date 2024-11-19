package com.example.burnify.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.lifecycle.ViewModelProvider
import com.example.burnify.NotificationHelper
import com.example.burnify.model.MagnetometerMeasurements
import com.example.burnify.model.MagnetometerSample
import com.example.burnify.viewmodel.MagnetometerViewModel

class MagnetometerService : Service(), SensorEventListener {

    // SensorManager per accedere ai sensori di sistema
    private lateinit var sensorManager: SensorManager
    private val samplesBatch = 20
    private var magnetometer: Sensor? = null

    // Intervallo di campionamento in millisecondi
    private val samplingInterval: Long = 250L

    // Contenitore per i dati del magnetometro
    private val magnetometerData = MagnetometerMeasurements()

    // Handler per eseguire aggiornamenti periodici
    private val handler = Handler(Looper.getMainLooper())
    private val sample = MagnetometerSample()

    // ViewModel per gestire i dati del magnetometro
    private lateinit var viewModel: MagnetometerViewModel

    override fun onCreate() {
        super.onCreate()
        println("Servizio Magnetometro inizializzato")

        // Configura la notifica per il Foreground Service
        startForegroundWithNotification()

        // Inizializza il SensorManager
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        // Inizializza il ViewModel
        viewModel = ViewModelProvider.AndroidViewModelFactory(application).create(
            MagnetometerViewModel::class.java
        )

        // Registra il listener del sensore
        magnetometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }

        // Avvia la raccolta dati
        startDataCollection()
    }

    private fun startForegroundWithNotification() {
        // Crea il canale di notifica per Android 8.0 e versioni successive
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "MagnetometerServiceChannel",
                "Servizio Magnetometro",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }

        // Crea la notifica
        val notificationHelper = NotificationHelper(this)
        val notification = notificationHelper.createServiceNotification("Magnetometer Service")
        startForeground(1002, notification)

        // Aggiorna o crea la notifica principale
        val groupNotification = notificationHelper.createGroupNotification()
        notificationHelper.notify(1000, groupNotification)
    }

    private fun startDataCollection() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                // Aggiorna i dati nel ViewModel
                viewModel.updateMagnetometerData(magnetometerData)

                // Ripianifica l'aggiornamento
                handler.postDelayed(this, samplingInterval)
            }
        }, samplingInterval)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        println("Servizio Magnetometro avviato con onStartCommand")
        return START_STICKY
    }

    private var samplesCount = 0
    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                // Imposta il campione
                sample.setSample(it.values[0], it.values[1], it.values[2])
                magnetometerData.addSample(applicationContext, sample)

                samplesCount++
                if (samplesCount >= samplesBatch) {
                    sendMagnetometerData()
                    samplesCount = 0
                }
            }
        }
    }

    private fun sendMagnetometerData() {
        val intent = Intent("com.example.burnify.MAGNETOMETER_DATA")
        intent.putExtra("data", magnetometerData)
        sendBroadcast(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Annulla la registrazione del listener
        sensorManager.unregisterListener(this)
        handler.removeCallbacksAndMessages(null)
        println("Servizio Magnetometro terminato")
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Non richiesto in questo esempio
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
