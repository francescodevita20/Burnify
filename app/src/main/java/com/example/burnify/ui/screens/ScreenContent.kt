package com.example.burnify.ui.screens

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.burnify.viewmodel.AccelerometerViewModel
import com.example.burnify.viewmodel.GyroscopeViewModel

@Composable
fun ScreenContent(
    selectedPage: String,
    accelerometerViewModel: AccelerometerViewModel = viewModel(),
    gyroscopeViewModel: GyroscopeViewModel = viewModel()
) {
    when (selectedPage) {
        "Today" -> TodayScreen()
        "Data" -> DataScreen(accelerometerViewModel= accelerometerViewModel,gyroscopeViewModel = gyroscopeViewModel)
        "Settings" -> SettingsScreen()
        else -> NotFoundScreen()
    }
}
