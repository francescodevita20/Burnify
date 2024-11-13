package com.example.burnify.presentation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text



@Composable
fun DataScreen(
    accelerometerViewModel: AccelerometerViewModel
) {
    val accelerometerData by accelerometerViewModel.accelerometerData.observeAsState()

    // Manteniamo una variabile per la ricezione dei dati dal broadcast
    var dataReceived by remember { mutableStateOf<AccelerometerMeasurements?>(null) }

    // Registriamo un BroadcastReceiver per ricevere i dati dal servizio
    val context = LocalContext.current

    val receiver = remember {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                intent?.let {
                    // Estrai i dati dall'Intent e aggiorna la variabile di stato
                    val data = it.getParcelableExtra("data") as? AccelerometerMeasurements
                    dataReceived = data
                }
            }
        }
    }

    // La registrazione del receiver avviene all'interno di DisposableEffect per evitare perdite di memoria
    DisposableEffect(context) {
        val filter = IntentFilter("com.example.burnify.ACCELEROMETER_DATA")

        // Registrazione dinamica con flag appropriato
        context.registerReceiver(
            receiver,
            filter,
            Context.RECEIVER_EXPORTED // A partire da Android 12, aggiungi questo flag
        )

        // Cleanup: deregistra il receiver quando non è più necessario
        onDispose {
            context.unregisterReceiver(receiver)
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
            color = MaterialTheme.colors.primary,
            text = "Data content"
        )

        // Se i dati sono stati ricevuti, mostriamo i dettagli
        dataReceived?.let { sample ->
            // Visualizza il numero di campioni ricevuti e altri dettagli
            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colors.primary,
                text = "Accelerometer Data:\n Samples: ${sample.getSamples().size}"
            )

            // Esempio di come mostrare i dati dettagliati
            val sampleValues = sample.getLastSample()?.getSampleValues() ?: "No data"
            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colors.primary,
                text = "Last Sample: $sampleValues"
            )
        } ?: run {
            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colors.primary,
                text = "Loading accelerometer data..."
            )
        }
    }
}

