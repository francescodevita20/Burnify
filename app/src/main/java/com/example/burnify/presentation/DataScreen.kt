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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text

@Composable
fun DataScreen(
    accelerometerViewModel: AccelerometerViewModel,
    compassViewModel: CompassViewModel,
    sharedDataViewModel: SharedDataViewModel = viewModel()
) {
    val accelerometerData = accelerometerViewModel.getAccelerometerData().observeAsState()
    val compassData = compassViewModel.getCompassData().observeAsState()

    // Use rememberSaveable with custom Savers
    val accelerometerMeasurements = sharedDataViewModel.getAccelerometerMeasurements()
    val compassMeasurements = sharedDataViewModel.getCompassMeasurements()

    val lastAngle = remember { mutableStateOf<Float?>(null) }
    val lastAccelerometer = remember { mutableStateOf<AccelerometerSample?>(null) }

    LaunchedEffect(compassData.value) {
        compassData.value?.let { sample ->
            if (lastAngle.value == null || lastAngle.value != sample.getAngle()) {
                lastAngle.value = sample.getAngle()
            }
        }
    }

    LaunchedEffect(accelerometerData.value) {
        accelerometerData.value?.let { accelerometerSample ->
            lastAccelerometer.value = accelerometerSample
            accelerometerMeasurements.addSample(accelerometerSample)
            compassData.value?.let { compassSample ->
                compassMeasurements.addSample(compassSample)
            }
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
            text = "Accelerometer Samples: ${accelerometerMeasurements.getSamples().size}"
        )

        Text(
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colors.primary,
            text = "Compass Samples: ${compassMeasurements.getSamples().size}"
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
