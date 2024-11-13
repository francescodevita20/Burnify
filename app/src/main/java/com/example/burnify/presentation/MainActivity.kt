package com.example.burnify.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels

class MainActivity : ComponentActivity() {


    private val accelerometerViewModel: AccelerometerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startService(Intent(this, AccelerometerService::class.java))

        setContent {
            WearApp(accelerometerViewModel = accelerometerViewModel)
        }
    }
}
