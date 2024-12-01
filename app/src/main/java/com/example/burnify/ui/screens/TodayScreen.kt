package com.example.burnify.ui.screens

import ActivityData
import CaloriesBurnedChart
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import com.example.burnify.processor.CaloriesDataProcessor
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.burnify.getSharedPreferences
import com.example.burnify.ui.components.HistogramActivityChart
import com.example.burnify.viewmodel.PredictedActivityViewModel


@Composable
fun TodayScreen(context: Context, viewModel: PredictedActivityViewModel) {
    // Variabile di stato per conservare le classi e i dati delle attività
    var classes by remember { mutableStateOf<List<String>>(emptyList()) }
    var caloriesData by remember { mutableStateOf<List<ActivityData>>(emptyList()) }
    val weight = (getSharedPreferences(context, "userdata")?.get("weight")).toString().toInt()
    val predictedActivityData by viewModel.predictedActivityData.observeAsState()

    // Chiamata per recuperare i dati solo una volta
    LaunchedEffect(Unit) {

        viewModel.loadPredictedActivityData()
    }

    // Aggiornamento delle classi e dei dati delle attività quando i dati predetti cambiano
    LaunchedEffect(predictedActivityData) {
        println("Valori ritrovati dal database: ${predictedActivityData?.toString()}")
        classes = predictedActivityData?.map { it.label.toString() } ?: emptyList()
        caloriesData = predictedActivityData?.map {

            ActivityData(
                hour = it.processedAt.toInt() ,
                activityType = it.label.toString(),
                caloriesBurned = CaloriesDataProcessor.processMeasurements(weight,it.label.toString(), 0.00555556f)
            )
        } ?: emptyList()
        println("CLASSI:" + classes.toString())
        println("ACTIVITY DATA:" + caloriesData.toString())
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary, // Usa colorScheme per Material3
            text = "Today's content"
        )

        Column {
            HistogramActivityChart(classes = classes)
        }

        Column {
            CaloriesBurnedChart(activityData = caloriesData)
        }
    }
}
