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

/**
 * Navbar Composable function that renders a row of navigation icons for different pages.
 *
 * @param onPageSelected A callback function that is triggered when a navigation icon is clicked.
 */
@Composable
fun Navbar(onPageSelected: (String) -> Unit) {
    // Row layout for the navigation bar, with spacing between icons
    Row(
        modifier = Modifier
            .fillMaxWidth() // Makes the row span the entire width of the screen
            .padding(8.dp), // Adds padding for better layout on smartphone screens
        horizontalArrangement = Arrangement.SpaceAround // Distributes icons evenly across the row
    ) {
        // Icon for the "Today" page
        IconButton(onClick = { onPageSelected("Today") }) {
            Icon(
                imageVector = Icons.Default.Today, // The icon representing "Today"
                contentDescription = "Today", // Description for accessibility
                tint = Color.Black // Sets the icon color to black (adjust based on theme)
            )
        }

        // Icon for the "Data" page
        IconButton(onClick = { onPageSelected("Data") }) {
            Icon(
                imageVector = Icons.Default.DateRange, // The icon representing "Data"
                contentDescription = "Data", // Description for accessibility
                tint = Color.Black // Sets the icon color to black (adjust based on theme)
            )
        }

        // Icon for the "Settings" page
        IconButton(onClick = { onPageSelected("Settings") }) {
            Icon(
                imageVector = Icons.Default.Settings, // The icon representing "Settings"
                contentDescription = "Settings", // Description for accessibility
                tint = Color.Black // Sets the icon color to black (adjust based on theme)
            )
        }
    }
}
