package com.example.burnify.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ScreenContent(selectedPage: String, accelerometerViewModel: AccelerometerViewModel = viewModel(), compassViewModel: CompassViewModel = viewModel()) {
    val accelerometerData = accelerometerViewModel.accelerometerData.observeAsState()
    val compassData = compassViewModel.compassDirection.observeAsState()

    when (selectedPage) {
        "Today" -> {
            Text(qq
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colors.primary,
                text = "Today's content"
            )
        }
        "Data" -> {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(50.dp)
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colors.primary,
                    text = "Data content"
                )

                // Mostra il numero di campioni
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colors.primary,
                    text = "Number of Accelerometer Samples: ${accelerometerData.value?.size ?: 0}"
                )

                // Mostra il primo dato campionato dell'accelerometro
                accelerometerData.value?.lastOrNull()?.let { sample ->
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colors.primary,
                        text = "Accelerometer Data:\nX: ${sample.x}, Y: ${sample.y}, Z: ${sample.z}"
                    )
                } ?: run {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colors.primary,
                        text = "Loading accelerometer data..."
                    )
                }

                // Mostra la direzione della bussola
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colors.primary,
                    text = "Compass Direction: ${compassData.value ?: 0}"
                )
            }
        }
        "Settings" -> {
            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colors.primary,
                text = "Settings content"
            )
        }
        else -> {
            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colors.error,
                text = "Page not found"
            )
        }
    }
}
