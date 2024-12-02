package com.example.burnify.ui.screens

import SettingsScreen
import TodayScreen
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.burnify.viewmodel.AccelerometerViewModel
import com.example.burnify.viewmodel.GyroscopeViewModel
import com.example.burnify.viewmodel.MagnetometerViewModel
import com.example.burnify.viewmodel.PredictedActivityViewModel

// Composable function to manage the content of the screen based on the selected page
@Composable
fun ScreenContent(
    selectedPage: String, // The selected page to display
    context: Context, // The context to pass to screens
    accelerometerViewModel: AccelerometerViewModel = viewModel(), // ViewModel for accelerometer data
    gyroscopeViewModel: GyroscopeViewModel = viewModel(), // ViewModel for gyroscope data
    magnetometerViewModel: MagnetometerViewModel = viewModel(), // ViewModel for magnetometer data
    predictedActivityViewModel: PredictedActivityViewModel = viewModel() // ViewModel for predicted activity data
) {
    // Based on the selectedPage, show the appropriate screen
    when (selectedPage) {
        "Today" -> TodayScreen(context, predictedActivityViewModel) // Display TodayScreen with the predicted activity ViewModel
        "Data" -> DataScreen(
            accelerometerViewModel = accelerometerViewModel, // Pass accelerometer data
            gyroscopeViewModel = gyroscopeViewModel, // Pass gyroscope data
            magnetometerViewModel = magnetometerViewModel // Pass magnetometer data
        )
        "Settings" -> SettingsScreen(context) // Display SettingsScreen
        else -> NotFoundScreen() // If an unknown page is selected, show NotFoundScreen
    }
}
