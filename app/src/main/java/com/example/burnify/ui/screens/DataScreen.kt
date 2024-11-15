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
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.burnify.model.AccelerometerMeasurements
import com.example.burnify.model.GyroscopeMeasurements
import com.example.burnify.model.MagnetometerMeasurements
import com.example.burnify.viewmodel.AccelerometerViewModel
import com.example.burnify.viewmodel.GyroscopeViewModel
import com.example.burnify.viewmodel.MagnetometerViewModel

@Composable
fun DataScreen(
    accelerometerViewModel: AccelerometerViewModel,
    gyroscopeViewModel: GyroscopeViewModel,
    magnetometerViewModel: MagnetometerViewModel
) {
    val context = LocalContext.current

    // Osserva i dati direttamente dai ViewModel
    val accelerometerData by accelerometerViewModel.accelerometerData.observeAsState()
    val gyroscopeData by gyroscopeViewModel.gyroscopeData.observeAsState()
    val magnetometerData by magnetometerViewModel.magnetometerData.observeAsState()

    // Register BroadcastReceivers
    DisposableEffect(context) {
        // Function to create a generic receiver
        fun createReceiver(onReceive: (Intent?) -> Unit): BroadcastReceiver {
            return object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    onReceive(intent)
                }
            }
        }

        // Create receivers for each sensor
        val accelerometerReceiver = createReceiver { intent ->
            intent?.getParcelableExtra<AccelerometerMeasurements>("data")?.let {
                accelerometerViewModel.updateAccelerometerData(it)
            }
        }

        val gyroscopeReceiver = createReceiver { intent ->
            intent?.getParcelableExtra<GyroscopeMeasurements>("data")?.let {
                gyroscopeViewModel.updateGyroscopeData(it)
            }
        }

        val magnetometerReceiver = createReceiver { intent ->
            intent?.getParcelableExtra<MagnetometerMeasurements>("data")?.let {
                magnetometerViewModel.updateMagnetometerData(it)
            }
        }

        // Register receivers with filters
        val filters = mapOf(
            accelerometerReceiver to IntentFilter("com.example.burnify.ACCELEROMETER_DATA"),
            gyroscopeReceiver to IntentFilter("com.example.burnify.GYROSCOPE_DATA"),
            magnetometerReceiver to IntentFilter("com.example.burnify.MAGNETOMETER_DATA")
        )

        filters.forEach { (receiver, filter) ->
            context.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
        }

        onDispose {
            filters.keys.forEach { context.unregisterReceiver(it) }
        }
    }

    // UI Layout
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
            text = "Sensor Data"
        )

        // Generic function for displaying sensor data
        @Composable
        fun displaySensorData(
            title: String,
            data: Any?,
            lastSample: () -> String
        ) {
            if (data != null) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary,
                    text = "$title:\n Samples: ${(data as? List<*>)?.size ?: "Unknown"}"
                )
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary,
                    text = "Last Sample: ${lastSample()}"
                )
            } else {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary,
                    text = "Loading $title data..."
                )
            }
        }

        // Display each sensor's data using ViewModel
        displaySensorData(
            title = "Accelerometer Data",
            data = accelerometerData?.getSamples(),
            lastSample = { accelerometerData?.getLastSample()?.getSampleValues().toString() ?: "No data" }
        )

        displaySensorData(
            title = "Gyroscope Data",
            data = gyroscopeData?.getSamples(),
            lastSample = { gyroscopeData?.getLastSample()?.getSampleValues().toString() ?: "No data" }
        )

        displaySensorData(
            title = "Magnetometer Data",
            data = magnetometerData?.getSamples(),
            lastSample = { magnetometerData?.getLastSample()?.getSampleValues().toString() ?: "No data" }
        )
    }
}
