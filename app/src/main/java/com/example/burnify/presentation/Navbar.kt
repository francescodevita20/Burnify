package com.example.burnify.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Icon



@Composable
fun Navbar(onPageSelected: (String) -> Unit) {
    // Ottieni il Context locale


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(2.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        IconButton(onClick = { onPageSelected("Today") }) {
            Icon(
                imageVector = Icons.Default.Today,
                contentDescription = "Today",
                tint = Color.White
            )
        }
        IconButton(onClick = {
            onPageSelected("Data")// Chiama la funzione per catturare i dati dell'accelerometro
        }) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = "Data",
                tint = Color.White
            )
        }
        IconButton(onClick = { onPageSelected("Settings") }) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = Color.White
            )
        }
    }
}
