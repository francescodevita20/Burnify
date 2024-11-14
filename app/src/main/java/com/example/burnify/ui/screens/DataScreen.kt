package com.example.burnify.ui.screens

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.burnify.model.AccelerometerMeasurements
import com.example.burnify.model.GyroscopeMeasurements
import com.example.burnify.model.MagnetometerMeasurements // Importa il modello per il magnetometro
import com.example.burnify.viewmodel.AccelerometerViewModel
import com.example.burnify.viewmodel.GyroscopeViewModel
import com.example.burnify.viewmodel.MagnetometerViewModel // Importa il ViewModel per il magnetometro

@Composable
fun DataScreen(
    accelerometerViewModel: AccelerometerViewModel,
    gyroscopeViewModel: GyroscopeViewModel,
    magnetometerViewModel: MagnetometerViewModel // ViewModel per il magnetometro
) {
    val accelerometerData by accelerometerViewModel.accelerometerData.observeAsState()
    val gyroscopeData by gyroscopeViewModel.gyroscopeData.observeAsState()
    val magnetometerData by magnetometerViewModel.magnetometerData.observeAsState() // Osserva i dati del magnetometro

    var accelerometerDataReceived by remember { mutableStateOf<AccelerometerMeasurements?>(null) }
    var gyroscopeDataReceived by remember { mutableStateOf<GyroscopeMeasurements?>(null) }
    var magnetometerDataReceived by remember { mutableStateOf<MagnetometerMeasurements?>(null) } // Variabile per i dati del magnetometro

    val context = LocalContext.current

    // BroadcastReceiver per i dati dell'accelerometro
    val accelerometerReceiver = remember {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                intent?.let {
                    val data = it.getParcelableExtra("data") as? AccelerometerMeasurements
                    accelerometerDataReceived = data
                }
            }
        }
    }

    // BroadcastReceiver per i dati del giroscopio
    val gyroscopeReceiver = remember {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                intent?.let {
                    val data = it.getParcelableExtra("data") as? GyroscopeMeasurements
                    gyroscopeDataReceived = data
                }
            }
        }
    }

    // BroadcastReceiver per i dati del magnetometro
    val magnetometerReceiver = remember {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                intent?.let {
                    val data = it.getParcelableExtra("data") as? MagnetometerMeasurements
                    magnetometerDataReceived = data
                }
            }
        }
    }

    // Registrazione dei BroadcastReceiver
    DisposableEffect(context) {
        val accelerometerFilter = IntentFilter("com.example.burnify.ACCELEROMETER_DATA")
        val gyroscopeFilter = IntentFilter("com.example.burnify.GYROSCOPE_DATA")
        val magnetometerFilter = IntentFilter("com.example.burnify.MAGNETOMETER_DATA") // Intent filter per il magnetometro

        context.registerReceiver(accelerometerReceiver, accelerometerFilter, Context.RECEIVER_EXPORTED)
        context.registerReceiver(gyroscopeReceiver, gyroscopeFilter, Context.RECEIVER_EXPORTED)
        context.registerReceiver(magnetometerReceiver, magnetometerFilter, Context.RECEIVER_EXPORTED) // Registra il receiver per il magnetometro

        onDispose {
            context.unregisterReceiver(accelerometerReceiver)
            context.unregisterReceiver(gyroscopeReceiver)
            context.unregisterReceiver(magnetometerReceiver) // Deregistra il receiver per il magnetometro
        }
    }

    // UI per mostrare i dati
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary,
            text = "Data content"
        )

        // Dati dell'accelerometro
        accelerometerDataReceived?.let { sample ->
            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary,
                text = "Accelerometer Data:\n Samples: ${sample.getSamples().size}"
            )
            val accelerometerSampleValues = sample.getLastSample()?.getSampleValues() ?: "No data"
            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary,
                text = "Last Accelerometer Sample: $accelerometerSampleValues"
            )
        } ?: run {
            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary,
                text = "Loading accelerometer data..."
            )
        }

        // Dati del giroscopio
        gyroscopeDataReceived?.let { sample ->
            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary,
                text = "Gyroscope Data:\n Samples: ${sample.getSamples().size}"
            )
            val gyroscopeSampleValues = sample.getLastSample()?.getSampleValues() ?: "No data"
            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary,
                text = "Last Gyroscope Sample: $gyroscopeSampleValues"
            )
        } ?: run {
            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary,
                text = "Loading gyroscope data..."
            )
        }

        // Dati del magnetometro
        magnetometerDataReceived?.let { sample ->
            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary,
                text = "Magnetometer Data:\n Samples: ${sample.getSamples().size}"
            )
            val magnetometerSampleValues = sample.getLastSample()?.getSampleValues() ?: "No data"
            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary,
                text = "Last Magnetometer Sample: $magnetometerSampleValues"
            )
        } ?: run {
            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary,
                text = "Loading magnetometer data..."
            )
        }
    }
}
