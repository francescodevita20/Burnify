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

// Composable function for the DataScreen
@Composable
fun DataScreen(
    accelerometerViewModel: AccelerometerViewModel,
    gyroscopeViewModel: GyroscopeViewModel,
    magnetometerViewModel: MagnetometerViewModel
) {
    // Local context for registering broadcast receivers
    val context = LocalContext.current

    // Observing sensor data from ViewModels
    val accelerometerData by accelerometerViewModel.accelerometerData.observeAsState()
    val gyroscopeData by gyroscopeViewModel.gyroscopeData.observeAsState()
    val magnetometerData by magnetometerViewModel.magnetometerData.observeAsState()

    // Register BroadcastReceivers for each sensor (Accelerometer, Gyroscope, Magnetometer)
    DisposableEffect(context) {
        // Generic function to create a receiver for sensor data
        fun createReceiver(onReceive: (Intent?) -> Unit): BroadcastReceiver {
            return object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    onReceive(intent)
                }
            }
        }

        // Creating receivers for each type of sensor
        val accelerometerReceiver = createReceiver { intent ->
            intent?.getParcelableExtra<AccelerometerMeasurements>("data")?.let {
                accelerometerViewModel.updateAccelerometerData(it) // Update accelerometer data in the ViewModel
            }
        }

        val gyroscopeReceiver = createReceiver { intent ->
            intent?.getParcelableExtra<GyroscopeMeasurements>("data")?.let {
                gyroscopeViewModel.updateGyroscopeData(it) // Update gyroscope data in the ViewModel
            }
        }

        val magnetometerReceiver = createReceiver { intent ->
            intent?.getParcelableExtra<MagnetometerMeasurements>("data")?.let {
                magnetometerViewModel.updateMagnetometerData(it) // Update magnetometer data in the ViewModel
            }
        }

        // Intent filters to receive data for each sensor type
        val filters = mapOf(
            accelerometerReceiver to IntentFilter("com.example.burnify.ACCELEROMETER_DATA"),
            gyroscopeReceiver to IntentFilter("com.example.burnify.GYROSCOPE_DATA"),
            magnetometerReceiver to IntentFilter("com.example.burnify.MAGNETOMETER_DATA")
        )

        // Registering the receivers with appropriate filters
        filters.forEach { (receiver, filter) ->
            context.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
        }

        // Cleanup: Unregister receivers when the composable is disposed
        onDispose {
            filters.keys.forEach { context.unregisterReceiver(it) }
        }
    }

    // UI Layout for displaying sensor data
    Column(
        modifier = Modifier
            .fillMaxWidth() // Fill available width
            .verticalScroll(rememberScrollState()) // Make the column scrollable
            .padding(16.dp), // Padding around the column
        verticalArrangement = Arrangement.spacedBy(10.dp) // Space between elements
    ) {
        // Title for the screen
        Text(
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary,
            text = "Sensor Data"
        )

        // Generic function for displaying sensor data
        @Composable
        fun displaySensorData(
            title: String, // Title for the sensor data
            data: Any?, // The data to display (sensor data)
            lastSample: () -> String // Function to display the last sample
        ) {
            if (data != null) {
                // If data is available, display the title and the number of samples
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary,
                    text = "$title:\n Samples: ${(data as? List<*>)?.size ?: "Unknown"}"
                )
                // Display the last sample
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary,
                    text = "Last Sample: ${lastSample()}"
                )
            } else {
                // If data is not yet available, display a loading message
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary,
                    text = "Loading $title data..."
                )
            }
        }

        // Display each sensor's data using ViewModels
        displaySensorData(
            title = "Accelerometer Data",
            data = accelerometerData?.getSamples(),
            lastSample = { accelerometerData?.getSamples()?.lastOrNull().toString() ?: "No data" }
        )

        displaySensorData(
            title = "Gyroscope Data",
            data = gyroscopeData?.getSamples(),
            lastSample = { gyroscopeData?.getSamples()?.lastOrNull().toString() ?: "No data" }
        )

        displaySensorData(
            title = "Magnetometer Data",
            data = magnetometerData?.getSamples(),
            lastSample = { magnetometerData?.getSamples()?.lastOrNull().toString() ?: "No data" }
        )
    }
}
