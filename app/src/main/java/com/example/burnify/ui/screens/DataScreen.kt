package com.example.burnify.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.burnify.util.getLastPredictionsFromSharedPreferences
import com.example.burnify.viewmodel.AccelerometerViewModel
import com.example.burnify.viewmodel.GyroscopeViewModel
import com.example.burnify.viewmodel.MagnetometerViewModel
import com.example.burnify.viewmodel.LastPredictionViewModel

import androidx.compose.ui.graphics.Color

@Composable
fun DataScreen(
    accelerometerViewModel: AccelerometerViewModel,
    gyroscopeViewModel: GyroscopeViewModel,
    magnetometerViewModel: MagnetometerViewModel,
    lastPredictionViewModel: LastPredictionViewModel
) {
    val context = LocalContext.current

    // Observing sensor data
    val accelerometerData by accelerometerViewModel.accelerometerData.observeAsState()
    val gyroscopeData by gyroscopeViewModel.gyroscopeData.observeAsState()
    val magnetometerData by magnetometerViewModel.magnetometerData.observeAsState()

    // Observing last prediction data
    val lastPredictionData by lastPredictionViewModel.lastPredictionData.observeAsState()

    // Retrieve last 5 predictions
    val lastPredictions = getLastPredictionsFromSharedPreferences(context, "last_predictions").take(5)

    // UI Layout
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color.White) // Background color set to white
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        Text(
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge,
            text = "Sensor Data",
            color = MaterialTheme.colorScheme.primary
        )

        // Sensor Data Cards
        SensorDataCard("Accelerometer Data", accelerometerData?.getSamples()?.size ?: 0)
        SensorDataCard("Gyroscope Data", gyroscopeData?.getSamples()?.size ?: 0)
        SensorDataCard("Magnetometer Data", magnetometerData?.getSamples()?.size ?: 0)

        // Last Prediction Card
        PredictionCard(lastPredictionData?.toString() ?: "No prediction available.")

        // Recent Activities Card
        RecentActivitiesCard(lastPredictions.map { it.toString() })
    }
}

@Composable
fun SensorDataCard(title: String, sampleCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White) // Set card background to white
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Samples: $sampleCount",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun PredictionCard(lastPredictionData: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White) // Set card background to white
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Last Prediction",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = lastPredictionData,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun RecentActivitiesCard(activities: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White) // Set card background to white
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Recent Activities",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (activities.isNotEmpty()) {
                activities.forEach { activity ->
                    Text(
                        text = "• $activity",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                Text(
                    text = "No recent activities available.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
