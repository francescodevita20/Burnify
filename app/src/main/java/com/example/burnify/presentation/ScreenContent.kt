package com.example.burnify.presentation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ScreenContent(
    selectedPage: String,
    accelerometerViewModel: AccelerometerViewModel = viewModel(),
    compassViewModel: CompassViewModel = viewModel()
) {
    when (selectedPage) {
        "Today" -> TodayScreen()
        "Data" -> DataScreen(accelerometerViewModel, compassViewModel)
        "Settings" -> SettingsScreen()
        else -> NotFoundScreen()
    }
}
