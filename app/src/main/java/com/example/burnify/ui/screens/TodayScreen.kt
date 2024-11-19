package com.example.burnify.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.burnify.ui.components.ActivityData
import com.example.burnify.ui.components.CaloriesBurnedChart
import com.example.burnify.ui.components.HistogramActivityChart

@Composable
fun TodayScreen() {

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

        val classes = listOf("a", "b", "a", "a", "a", "c", "d", "e", "c", "c", "c", "e")

        Column {
            HistogramActivityChart(classes = classes)
        }
        val activityData = listOf(
            ActivityData(hour = 0, activityType = "riposo", caloriesBurned = 50f),
            ActivityData(hour = 1, activityType = "riposo", caloriesBurned = 40f),
            ActivityData(hour = 1, activityType = "riposo", caloriesBurned = 30f),
            ActivityData(hour = 8, activityType = "corsa", caloriesBurned = 300f),
            ActivityData(hour = 10, activityType = "camminata", caloriesBurned = 150f),
            ActivityData(hour = 23, activityType = "palestra", caloriesBurned = 400f)
        )
        Column{
        CaloriesBurnedChart(activityData = activityData)

        }

    }
}
