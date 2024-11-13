package com.example.burnify.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun Navbar(onPageSelected: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),  // Maggiore padding per layout smartphone
        horizontalArrangement = Arrangement.SpaceAround // Adatta la spaziatura ai dispositivi più grandi
    ) {
        IconButton(onClick = { onPageSelected("Today") }) {
            Icon(
                imageVector = Icons.Default.Today,
                contentDescription = "Today",
                tint = Color.Black // Cambia colore per un layout smartphone, a seconda del tema
            )
        }
        IconButton(onClick = {
            onPageSelected("Data") // Funzione per navigare o gestire i dati
        }) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = "Data",
                tint = Color.Black
            )
        }
        IconButton(onClick = { onPageSelected("Settings") }) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = Color.Black
            )
        }
    }
}
