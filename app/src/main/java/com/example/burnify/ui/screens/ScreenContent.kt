package com.example.burnify.ui.screens

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.burnify.viewmodel.AccelerometerViewModel
import com.example.burnify.viewmodel.GyroscopeViewModel
import com.example.burnify.viewmodel.MagnetometerViewModel

@Composable
fun ScreenContent(
    selectedPage: String,
    accelerometerViewModel: AccelerometerViewModel = viewModel(),
    gyroscopeViewModel: GyroscopeViewModel = viewModel(),
    magnetometerViewModel: MagnetometerViewModel = viewModel()
) {
    when (selectedPage) {
        "Today" -> TodayScreen()
        "Data" -> DataScreen(accelerometerViewModel= accelerometerViewModel,gyroscopeViewModel = gyroscopeViewModel, magnetometerViewModel = magnetometerViewModel)
        "Settings" -> SettingsScreen()
        else -> NotFoundScreen()
    }
}
