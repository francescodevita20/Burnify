package com.example.burnify.ui.screens

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.burnify.viewmodel.AccelerometerViewModel
import com.example.burnify.viewmodel.GyroscopeViewModel
import com.example.burnify.viewmodel.MagnetometerViewModel
import com.example.burnify.viewmodel.PredictedActivityViewModel

@Composable
fun ScreenContent(
    selectedPage: String,
    context: Context,
    accelerometerViewModel: AccelerometerViewModel = viewModel(),
    gyroscopeViewModel: GyroscopeViewModel = viewModel(),
    magnetometerViewModel: MagnetometerViewModel = viewModel(),
    predictedActivityViewModel: PredictedActivityViewModel = viewModel()
) {
    when (selectedPage) {
        "Today" -> TodayScreen(context,predictedActivityViewModel)
        "Data" -> DataScreen(accelerometerViewModel= accelerometerViewModel,gyroscopeViewModel = gyroscopeViewModel, magnetometerViewModel = magnetometerViewModel)
        "Settings" -> SettingsScreen(context)
        else -> NotFoundScreen()
    }
}
