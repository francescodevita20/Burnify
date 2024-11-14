package com.example.burnify

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.burnify.ui.screens.ScreenContent
import com.example.burnify.ui.theme.BurnifyTheme
import com.example.burnify.viewmodel.AccelerometerViewModel
import com.example.burnify.viewmodel.GyroscopeViewModel
import com.example.burnify.viewmodel.MagnetometerViewModel

@Composable
fun App(accelerometerViewModel: AccelerometerViewModel , gyroscopeViewModel: GyroscopeViewModel,magnetometerViewModel: MagnetometerViewModel) {
    var selectedPage by remember { mutableStateOf("Today") }

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
                        ScreenContent(selectedPage = selectedPage, accelerometerViewModel = accelerometerViewModel, gyroscopeViewModel = gyroscopeViewModel, magnetometerViewModel = magnetometerViewModel)
                    }

                    // Navbar sempre visibile in basso
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Sostituisci `Navbar` con un layout di icone personalizzato
                        IconButton(onClick = { selectedPage = "Today" }) {
                            Icon(
                                imageVector = Icons.Default.Today,
                                contentDescription = "Today",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = { selectedPage = "Data" }) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Data",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = { selectedPage = "Settings" }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
