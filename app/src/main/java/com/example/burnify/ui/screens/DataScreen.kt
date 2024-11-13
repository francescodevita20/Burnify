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
import com.example.burnify.model.GyroscopeMeasurements  // Importa il modello per il giroscopio
import com.example.burnify.viewmodel.AccelerometerViewModel
import com.example.burnify.viewmodel.GyroscopeViewModel

@Composable
fun DataScreen(
    accelerometerViewModel: AccelerometerViewModel,
    gyroscopeViewModel: GyroscopeViewModel
) {
    val accelerometerData by accelerometerViewModel.accelerometerData.observeAsState()
    val gyroscopeData by gyroscopeViewModel.gyroscopeData.observeAsState()  // Osserva i dati del giroscopio

    // Manteniamo variabili per la ricezione dei dati dal broadcast
    var accelerometerDataReceived by remember { mutableStateOf<AccelerometerMeasurements?>(null) }
    var gyroscopeDataReceived by remember { mutableStateOf<GyroscopeMeasurements?>(null) }

    // Registriamo un BroadcastReceiver per ricevere i dati dal servizio dell'accelerometro
    val context = LocalContext.current

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

    // Registriamo un BroadcastReceiver per ricevere i dati dal servizio del giroscopio
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

    // La registrazione dei receivers avviene all'interno di DisposableEffect per evitare perdite di memoria
    DisposableEffect(context) {
        val accelerometerFilter = IntentFilter("com.example.burnify.ACCELEROMETER_DATA")
        val gyroscopeFilter = IntentFilter("com.example.burnify.GYROSCOPE_DATA")

        // Registrazione dinamica con flag appropriato
        context.registerReceiver(
            accelerometerReceiver,
            accelerometerFilter,
            Context.RECEIVER_EXPORTED // A partire da Android 12, aggiungi questo flag
        )

        context.registerReceiver(
            gyroscopeReceiver,
            gyroscopeFilter,
            Context.RECEIVER_EXPORTED // A partire da Android 12, aggiungi questo flag
        )

        // Cleanup: deregistra i receivers quando non sono più necessari
        onDispose {
            context.unregisterReceiver(accelerometerReceiver)
            context.unregisterReceiver(gyroscopeReceiver)
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
            color = MaterialTheme.colorScheme.primary, // Usa colorScheme per Material3
            text = "Data content"
        )

        // Se i dati accelerometro sono stati ricevuti, mostriamo i dettagli
        accelerometerDataReceived?.let { sample ->
            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary,
                text = "Accelerometer Data:\n Samples: ${sample.getSamples().size}"
            )

            // Mostra il valore dell'ultimo campione
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

        // Se i dati giroscopio sono stati ricevuti, mostriamo i dettagli
        gyroscopeDataReceived?.let { sample ->
            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary,
                text = "Gyroscope Data:\n Samples: ${sample.getSamples().size}"
            )

            // Mostra il valore dell'ultimo campione
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
    }
}
