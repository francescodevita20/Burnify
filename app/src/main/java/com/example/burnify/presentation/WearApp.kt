package com.example.burnify.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.wear.compose.material.*
import com.example.burnify.presentation.theme.BurnifyTheme



@Composable
fun WearApp(accelerometerViewModel: AccelerometerViewModel) {
    var selectedPage by remember { mutableStateOf("Today") }

    BurnifyTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Contenuto della pagina che può scrollare
                Box(
                    modifier = Modifier
                        .weight(1f) // Occupa lo spazio rimanente sopra la Navbar
                        .fillMaxWidth()
                ) {
                    ScreenContent(selectedPage = selectedPage, accelerometerViewModel = accelerometerViewModel)
                }

                // Navbar sempre visibile in basso
                Navbar(onPageSelected = { page -> selectedPage = page })
            }
        }
    }
}

