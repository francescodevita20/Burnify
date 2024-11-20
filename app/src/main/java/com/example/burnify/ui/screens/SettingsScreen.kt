package com.example.burnify.ui.screens

import android.content.Context
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.burnify.getSharedPreferences
import com.example.burnify.setSharedPreferences

@Composable
fun SettingsScreen(context: Context) {
    // State for the selected mode
    var selectedMode by remember { mutableStateOf("Maximum Battery Saving") }
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Male") }

    // Available options
    val modes = listOf("maxbatterysaving", "maxaccuracy")
    val genders = listOf("Male", "Female")

    // Retrieve the saved mode and user data on initial load
    LaunchedEffect(Unit) {
        val savedSettings = getSharedPreferences(context, "setting")
        val savedMode = savedSettings?.get("workingmode") as? String
        if (savedMode != null) {
            selectedMode = savedMode
        }

        val savedUserData = getSharedPreferences(context, "userdata") ?: emptyMap()
        weight = savedUserData["weight"] as? String ?: ""
        height = savedUserData["height"] as? String ?: ""
        age = savedUserData["age"] as? String ?: ""
        gender = savedUserData["gender"] as? String ?: "Male"
    }

    fun updateUserData(key: String, value: String) {
        // Retrieve the existing data
        val currentData = getSharedPreferences(context, "userdata")?.toMutableMap() ?: mutableMapOf()
        // Update the specific key
        currentData[key] = value
        // Save the updated map
        setSharedPreferences(context, currentData, "userdata")
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

        // User data fields
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "User Information", style = MaterialTheme.typography.titleMedium)

        // Weight input
        Text(text = "Weight (kg)", style = MaterialTheme.typography.bodyMedium)
        BasicTextField(
            value = weight,
            onValueChange = {
                weight = it
                updateUserData("weight", it)
            },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .border(1.dp, Color.Gray)
                .padding(8.dp)
        )

        // Height input
        Text(text = "Height (cm)", style = MaterialTheme.typography.bodyMedium)
        BasicTextField(
            value = height,
            onValueChange = {
                height = it
                updateUserData("height", it)
            },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .border(1.dp, Color.Gray)
                .padding(8.dp)
        )

        // Age input
        Text(text = "Age", style = MaterialTheme.typography.bodyMedium)
        BasicTextField(
            value = age,
            onValueChange = {
                age = it
                updateUserData("age", it)
            },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .border(1.dp, Color.Gray)
                .padding(8.dp)
        )

        // Gender selector
        Text(text = "Gender", style = MaterialTheme.typography.bodyMedium)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            genders.forEach { option ->
                Row(
                    modifier = Modifier.clickable {
                        gender = option
                        updateUserData("gender", option)
                    },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = gender == option,
                        onClick = {
                            gender = option
                            updateUserData("gender", option)
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = option, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }


    }
}
