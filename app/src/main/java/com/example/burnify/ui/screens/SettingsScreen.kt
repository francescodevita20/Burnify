import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.burnify.util.getSharedPreferences
import com.example.burnify.util.setSharedPreferences

@Composable
fun SettingsScreen(context: Context) {
    var selectedMode by remember { mutableStateOf("Maximum Battery Saving") }
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Male") }

    val modes = listOf("maxbatterysaving", "maxaccuracy")
    val genders = listOf("Male", "Female")

    LaunchedEffect(Unit) {
        // Load settings from SharedPreferences
        val savedSettings = getSharedPreferences(context, "settings", "settings_key")
        val savedMode = savedSettings?.get("workingmode") as? String

        if (savedMode != null) {
            // If 'workingmode' exists, display the saved mode
            selectedMode = savedMode
        } else {
            // If 'workingmode' is missing, save the default "maxaccuracy"
            setSharedPreferences(
                context,
                mapOf("workingmode" to "maxaccuracy"),
                "settings",
                "settings_key"
            )
            selectedMode = "maxaccuracy" // Update the UI to show the default mode
        }

        // Load user data for weight, height, age, and gender
        val savedUserData = getSharedPreferences(context, "userdata", "user_data_key") ?: emptyMap()
        weight = savedUserData["weight"]?.toString() ?: ""
        height = savedUserData["height"]?.toString() ?: ""
        age = savedUserData["age"]?.toString() ?: ""
        gender = savedUserData["gender"] as? String ?: "Male"
    }

    fun updateUserData(key: String, value: String) {
        val currentData = getSharedPreferences(context, "userdata", "user_data_key")?.toMutableMap() ?: mutableMapOf()
        currentData[key] = value
        setSharedPreferences(context, currentData, "userdata", "user_data_key")
        println("Updated user data for $key: $value") // Added log
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White) // Imposta sfondo bianco
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary,
            text = "Settings",
            style = MaterialTheme.typography.titleLarge
        )
        // Card per la selezione della modalità
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Select Mode",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

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
                                    "settings",
                                    "settings_key"
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
                                    "settings",
                                    "settings_key"
                                )
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = mode, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        // Card per le informazioni utente
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "User Information", style = MaterialTheme.typography.titleMedium)

                Spacer(modifier = Modifier.height(8.dp))
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
                        .background(Color.White)
                        .padding(8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))
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
                        .background(Color.White)
                        .padding(8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))
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
                        .background(Color.White)
                        .padding(8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))
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
    }
}
