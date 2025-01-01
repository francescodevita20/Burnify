package com.example.burnify

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.burnify.ui.components.Navbar
import com.example.burnify.ui.screens1.ScreenContent
import com.example.burnify.ui.theme.BurnifyTheme
import com.example.burnify.viewmodel.AccelerometerViewModel
import com.example.burnify.viewmodel.GyroscopeViewModel
import com.example.burnify.viewmodel.LastPredictionViewModel
import com.example.burnify.viewmodel.MagnetometerViewModel
import com.example.burnify.viewmodel.PredictedActivityViewModel

@Composable
fun App(accelerometerViewModel: AccelerometerViewModel, gyroscopeViewModel: GyroscopeViewModel, magnetometerViewModel: MagnetometerViewModel,predictedActivityViewModel: PredictedActivityViewModel,lastPredictionViewModel: LastPredictionViewModel) {
    var selectedPage by remember { mutableStateOf("Today") }
    val context = LocalContext.current
    BurnifyTheme {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            color = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Contenuto della pagina principale
                    Box(
                        modifier = Modifier
                            .weight(1f) // Occupa lo spazio rimanente sopra la Navbar
                            .fillMaxWidth()
                    ) {
                        ScreenContent(
                            selectedPage = selectedPage,
                            accelerometerViewModel = accelerometerViewModel,
                            gyroscopeViewModel = gyroscopeViewModel,
                            magnetometerViewModel = magnetometerViewModel,
                            predictedActivityViewModel = predictedActivityViewModel,
                            lastPredictionViewModel = lastPredictionViewModel,
                            context = context
                        )
                    }

                    // Navbar sempre visibile in basso
                    Navbar(onPageSelected = { selectedPage = it })
                }
            }
        }
    }
}
