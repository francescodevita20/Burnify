package com.example.burnify.ui.screens

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.burnify.getSharedPreferences
import com.example.burnify.setSharedPreferences

@Composable
fun SettingsScreen(context: Context) {
    // State for the selected mode
    var selectedMode by remember { mutableStateOf("Maximum Battery Saving") }

    // Available options
    val modes = listOf("maxbatterysaving", "maxaccuracy")

    // Retrieve the saved mode on initial load
    LaunchedEffect(Unit) {
        val savedSettings = getSharedPreferences(context, "setting")
        val savedMode = savedSettings?.get("workingmode") as? String
        if (savedMode != null) {
            selectedMode = savedMode
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Select Mode",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Mode selector
        modes.forEach { mode ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable {
                        selectedMode = mode
                        // Update the SharedPreferences when a mode is selected
                        setSharedPreferences(
                            context,
                            mapOf("workingmode" to selectedMode),
                            "setting"
                        )
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedMode == mode,
                    onClick = {
                        selectedMode = mode
                        // Update the SharedPreferences when a mode is selected
                        setSharedPreferences(
                            context,
                            mapOf("workingmode" to selectedMode),
                            "setting"
                        )
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = mode, style = MaterialTheme.typography.bodyMedium)
            }
        }

        // Display the selected mode
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Selected Mode: $selectedMode",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
