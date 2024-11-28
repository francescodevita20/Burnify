package com.example.burnify.service

import SensorDataManager
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
import com.example.burnify.model.AccelerometerMeasurements
import com.example.burnify.model.AccelerometerSample
import com.example.burnify.viewmodel.AccelerometerViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AccelerometerService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    private var samplingRateInMillis: Long = 1000 // Default value: 1 second
    private var samplesBatch: Int = 100 // Default value: 64 samples per batch

    private val accelerometerData = AccelerometerMeasurements()
    private val handler = Handler(Looper.getMainLooper())
    private val sample = AccelerometerSample()

    private lateinit var viewModel: AccelerometerViewModel

    private var samplesCount = 0
    override fun onCreate() {
        super.onCreate()

        println("Servizio Accelerometro inizializzato")

        // Configura la notifica per il Foreground Service
        startForegroundWithNotification()

        // Inizializza il SensorManager
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        // Inizializza il ViewModel
        viewModel = ViewModelProvider.AndroidViewModelFactory(application).create(AccelerometerViewModel::class.java)

        // Impostazioni di "working mode"
        val sharedPreferences = getSharedPreferences("setting", Context.MODE_PRIVATE)
        val workingMode = sharedPreferences.getString("workingmode", "maxaccuracy") ?: "maxaccuracy"
        setSamplingRateAndBatchSize(workingMode)

        println("Servizio avviato con Sampling Rate: ${samplingRateInMillis}ms, Batch Size: $samplesBatch")

        // Registra il listener del sensore
        accelerometer?.let {
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
        val channelId = "AccelerometerServiceChannel"
        val channelName = "Servizio Accelerometro"
        val notificationId = 1001

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

        // Creazione notifica specifica per Accelerometer Service
        val accelerometerNotification = notificationHelper.createServiceNotification("Accelerometer Service")
        notificationHelper.notify(notificationId, accelerometerNotification)

        // Creazione e pubblicazione della notifica di gruppo
        val groupNotification = notificationHelper.createGroupNotification()
        notificationHelper.notify(1000, groupNotification)

        // Avvia in foreground con la notifica specifica
        startForeground(notificationId, accelerometerNotification)
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Imposta sampling rate da intent, se presente
        samplingRateInMillis = (intent?.getDoubleExtra("samplingRateInSeconds", 1.0)?.times(1000))?.toLong() ?: samplingRateInMillis
        println("Servizio avviato con Sampling Rate: ${samplingRateInMillis}ms")

        return START_STICKY
    }

    private fun startDataCollection() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                // Verifica se il servizio è in pausa, se sì, non aggiornare i dati
                    viewModel.updateAccelerometerData(accelerometerData)
                    // Ripianifica l'aggiornamento
                    handler.postDelayed(this, samplingRateInMillis)
                    //QUESTA E' LA RIGA CHE PORTA PROBLEMI DI SINCRONIZZAZIONE
            }
        }, samplingRateInMillis)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (SensorDataManager.accelerometerIsFilled) {
            println("Il servizio accelerometro è in pausa, aspetta gli altri servizi")
            return}
else{
        event?.let {
            if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                sample.setSample(it.values[0], it.values[1], it.values[2])
                accelerometerData.addSample(applicationContext, sample)

                samplesCount++
                if (samplesCount >= samplesBatch) {
                    // Solo quando il batch è pieno, invia i dati e metti in pausa
                    sendAccelerometerData() // Riprende il campionamento dopo la pausa // Pausa di 5 secondi prima di riprendere
                    samplesCount = 0
                }
            }
        }
    }

    }

    private fun sendAccelerometerData() {
        val intent = Intent("com.example.burnify.ACCELEROMETER_DATA")
        intent.putExtra("data", accelerometerData)
        sendBroadcast(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        handler.removeCallbacksAndMessages(null)
        println("Servizio Accelerometro terminato")
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Non richiesto in questo esempio
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
