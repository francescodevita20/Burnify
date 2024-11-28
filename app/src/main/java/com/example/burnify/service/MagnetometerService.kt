package com.example.burnify.service

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

    private lateinit var sensorManager: SensorManager
    private var magnetometer: Sensor? = null

    private var samplingRateInMillis: Long = 1000 // Default value: 1 second
    private var samplesBatch: Int = 100 // Default value: 64 samples per batch

    private val magnetometerData = MagnetometerMeasurements()
    private val handler = Handler(Looper.getMainLooper())
    private val sample = MagnetometerSample()

    private lateinit var viewModel: MagnetometerViewModel

    private var samplesCount = 0

    override fun onCreate() {
        super.onCreate()

        println("Servizio Magnetometro inizializzato")

        // Configura la notifica per il Foreground Service
        startForegroundWithNotification()

        // Inizializza il SensorManager
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        // Inizializza il ViewModel
        viewModel = ViewModelProvider.AndroidViewModelFactory(application).create(MagnetometerViewModel::class.java)

        // Impostazioni di "working mode"
        val sharedPreferences = getSharedPreferences("setting", Context.MODE_PRIVATE)
        val workingMode = sharedPreferences.getString("workingmode", "maxaccuracy") ?: "maxaccuracy"
        setSamplingRateAndBatchSize(workingMode)

        println("Servizio avviato con Sampling Rate: ${samplingRateInMillis}ms, Batch Size: $samplesBatch")

        // Registra il listener del sensore
        magnetometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }

        // Avvia la raccolta dati
        startDataCollection()
    }

    private fun setSamplingRateAndBatchSize(workingMode: String) {
        when (workingMode) {
            "maxbatterysaving" -> {
                samplingRateInMillis = 1000 // 1 secondo
                samplesBatch = 100
            }
            "maxaccuracy" -> {
                samplingRateInMillis = 250 // 250 ms per massima precisione
                samplesBatch = 50
            }
            else -> {
                samplingRateInMillis = 1000 // Default 1 secondo
                samplesBatch = 100
            }
        }
    }
    private fun startForegroundWithNotification() {
        val channelId = "MagnetometerServiceChannel"
        val channelName = "Servizio Magnetometro"
        val notificationId = 1003

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }

        val notificationHelper = NotificationHelper(this)

        // Creazione notifica specifica per Gyroscope Service
        val magnetometerNotification = notificationHelper.createServiceNotification("Magnetometer Service")
        notificationHelper.notify(notificationId, magnetometerNotification)

        // Creazione e pubblicazione della notifica di gruppo
        val groupNotification = notificationHelper.createGroupNotification()
        notificationHelper.notify(1000, groupNotification)

        // Avvia in foreground con la notifica specifica
        startForeground(notificationId, magnetometerNotification)
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        samplingRateInMillis = (intent?.getDoubleExtra("samplingRateInSeconds", 1.0)?.times(1000))?.toLong() ?: samplingRateInMillis
        println("Servizio avviato con Sampling Rate: ${samplingRateInMillis}ms")

        return START_STICKY
    }

    private fun startDataCollection() {
        handler.postDelayed(object : Runnable {
            override fun run() {

                    viewModel.updateMagnetometerData(magnetometerData)
                    handler.postDelayed(this, samplingRateInMillis)
                }
        }, samplingRateInMillis)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (SensorDataManager.magnetometerIsFilled) {
            println("Il servizio accelerometro è in pausa, aspetta gli altri servizi")
            return}
        else{
        event?.let {
            if (it.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                sample.setSample(it.values[0], it.values[1], it.values[2])
                magnetometerData.addSample(applicationContext, sample)

                samplesCount++
                if (samplesCount >= samplesBatch) {

                    sendMagnetometerData()
                    samplesCount = 0
                }
            }
        }
    }}

    private fun sendMagnetometerData() {
        val intent = Intent("com.example.burnify.MAGNETOMETER_DATA")
        intent.putExtra("data", magnetometerData)
        sendBroadcast(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        handler.removeCallbacksAndMessages(null)
        println("Servizio Magnetometro terminato")
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onBind(intent: Intent?): IBinder? = null
}
