package com.example.burnify.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.burnify.util.getLastPredictionsFromSharedPreferences
import com.example.burnify.viewmodel.LastPredictionViewModel

@Composable
fun DataScreen(
    lastPredictionViewModel: LastPredictionViewModel
) {
    val context = LocalContext.current

    // Observing last prediction data
    val lastPredictionData by lastPredictionViewModel.lastPredictionData.observeAsState()

    // Retrieve last 5 predictions from SharedPreferences
    val lastPredictions = getLastPredictionsFromSharedPreferences(context, "predictions").take(5)

    // Main Column to arrange all the cards vertically
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
    ) {
        // Last Prediction Card
        PredictionCard(lastPredictionData?.toString() ?: "No prediction available.")

        Spacer(modifier = Modifier.height(16.dp)) // Add space between the cards

        // Recent Activities Card
        RecentActivitiesCard(lastPredictions.map { it.toString() })
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
