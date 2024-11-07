package com.example.burnify.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text

@Composable
fun DataScreen(
    accelerometerViewModel: AccelerometerViewModel,
    compassViewModel: CompassViewModel
) {
    val accelerometerData = accelerometerViewModel.getAccelerometerData().observeAsState()
    val compassData = compassViewModel.getCompassData().observeAsState()
    val accelerometerMeasurements = remember { AccelerometerMeasurements() }
    val compassMeasurements = remember { CompassMeasurements() }  // Oggetto per salvare gli angoli della bussola

    // Creazione di una variabile di stato per memorizzare l'ultimo angolo
    val lastAngle = remember { mutableStateOf<Float?>(null) }

    // Se il valore dell'angolo cambia, stampalo
    LaunchedEffect(compassData.value) {
        compassData.value?.let { sample ->
            if (lastAngle.value == null || lastAngle.value != sample.getAngle()) {
                println("Compass Data Changed: Angle: ${sample.getAngle()}")
                lastAngle.value = sample.getAngle()// Aggiorna l'ultimo angolo
            }
        }
    }

    // Creazione di una variabile di stato per memorizzare l'ultimo campione accelerometrico
    val lastAccelerometer = remember { mutableStateOf<AccelerometerSample?>(null) }

    LaunchedEffect(accelerometerData.value) {
        accelerometerData.value?.let { sample ->
            // Aggiungi il campione accelerometrico
            accelerometerMeasurements.addSample(sample)

            // Aggiungi anche l'angolo corrente della bussola
            compassData.value?.let { compassSample ->
                compassMeasurements.addSample(compassSample)  // Salva l'angolo della bussola
            }

            println("Accelerometer Data Changed: X: ${sample.getX()}, Y: ${sample.getY()}, Z: ${sample.getZ()}")
            lastAccelerometer.value = sample // Aggiorna l'ultimo campione
        }
    }

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

        Text(
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colors.primary,
            text = "Number of Accelerometer Samples: ${accelerometerMeasurements.getSamples().size}"
        )

        Text(
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colors.primary,
            text = "Number of Compass Samples: ${compassMeasurements.getSamples().size}"
        )

        accelerometerData.value?.let { sample ->
            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colors.primary,
                text = "Accelerometer Data:\n ${sample.getX()} ${sample.getY()} ${sample.getZ()}"
            )
        } ?: run {
            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colors.primary,
                text = "Loading accelerometer data..."
            )
        }

        // Visualizzazione dei dati della bussola
        compassData.value?.let { sample ->
            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colors.primary,
                text = "Compass Data:\nAngle: ${sample.getAngle()}"
            )
        } ?: run {
            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colors.primary,
                text = "Loading compass data..."
            )
        }
    }
}
