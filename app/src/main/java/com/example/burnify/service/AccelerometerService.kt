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
import com.example.burnify.model.AccelerometerMeasurements
import com.example.burnify.model.AccelerometerSample
import com.example.burnify.scheduleDatabaseCleanup
import com.example.burnify.viewmodel.AccelerometerViewModel

class AccelerometerService : Service(), SensorEventListener {

    // SensorManager per accedere ai sensori di sistema
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    // Variabili per il sampling rate e il batch size
    private var samplingRateInMillis: Long = 1000 // Default value: 1 second
    private var samplesBatch: Int = 20 // Default value: 20 samples per batch

    // Contenitore per i dati dell'accelerometro
    private val accelerometerData = AccelerometerMeasurements()

    // Handler per eseguire aggiornamenti periodici
    private val handler = Handler(Looper.getMainLooper())
    private val sample = AccelerometerSample()

    // ViewModel per gestire i dati dell'accelerometro
    private lateinit var viewModel: AccelerometerViewModel

    override fun onCreate() {
        super.onCreate()

        println("Servizio Accelerometro inizializzato")
        scheduleDatabaseCleanup(applicationContext)

        // Configura la notifica per il Foreground Service
        startForegroundWithNotification()

        // Inizializza il SensorManager
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        // Inizializza il ViewModel
        viewModel = ViewModelProvider.AndroidViewModelFactory(application).create(
            AccelerometerViewModel::class.java
        )

        // Leggi il "workingmode" dalle SharedPreferences
        val sharedPreferences = getSharedPreferences("setting", Context.MODE_PRIVATE)
        val workingMode = sharedPreferences.getString("workingmode", "maxaccuracy") ?: "maxaccuracy"

        // Imposta il sampling rate e il batch size in base al "workingmode"
        when (workingMode) {
            "maxbatterysaving" -> {
                samplingRateInMillis = 1000 // Ad esempio, 5 secondi
                samplesBatch = 64 // Maggiore batch size per risparmiare batteria
            }
            "maxaccuracy" -> {
                samplingRateInMillis = 250 // Ad esempio, 500 ms per massima precisione
                samplesBatch = 32 // Minore batch size per una maggiore accuratezza
            }
            else -> {
                samplingRateInMillis = 1000 // Valore predefinito di 1 secondo
                samplesBatch = 64 // Batch di default
            }
        }

        println("Servizio avviato con Sampling Rate: ${samplingRateInMillis}ms, Batch Size: $samplesBatch")

        // Registra il listener del sensore
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    private fun startForegroundWithNotification() {
        // Crea il canale di notifica per Android 8.0 e versioni successive
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "AccelerometerServiceChannel",
                "Servizio Accelerometro",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }

        // Crea la notifica
        val notificationHelper = NotificationHelper(this)
        val notification = notificationHelper.createServiceNotification("Accelerometer Service")

        // Avvia il servizio come Foreground Service
        startForeground(1001, notification)

        val groupNotification = notificationHelper.createGroupNotification()
        notificationHelper.notify(1000, groupNotification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Avvia la raccolta dati
        startDataCollection()

        return START_STICKY
    }

    private fun startDataCollection() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                // Aggiorna i dati nel ViewModel
                viewModel.updateAccelerometerData(accelerometerData)

                // Ripianifica l'aggiornamento
                handler.postDelayed(this, samplingRateInMillis)
            }
        }, samplingRateInMillis)
    }

    private var samplesCount = 0
    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                sample.setSample(it.values[0], it.values[1], it.values[2])
                accelerometerData.addSample(applicationContext, sample)

                samplesCount++
                if (samplesCount >= samplesBatch) {
                    sendAccelerometerData()
                    samplesCount = 0
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
