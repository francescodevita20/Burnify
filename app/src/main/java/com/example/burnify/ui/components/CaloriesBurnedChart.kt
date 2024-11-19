package com.example.burnify.ui.components

import android.graphics.Color
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.components.XAxis
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

// Modello dati per rappresentare le attività
data class ActivityData(
    val hour: Int,            // Ora della giornata (0-23)
    val activityType: String, // Tipo di attività svolta (es. "corsa", "camminata")
    val caloriesBurned: Float // Calorie bruciate in quell'ora
)

@Composable
fun CaloriesBurnedChart(activityData: List<ActivityData>) {
    // Filtro per ore valide (0-23)
    val validData = activityData.filter { it.hour in 0..23 }

    // Raggruppa i dati per ora e somma le calorie
    val groupedData = validData
        .groupBy { it.hour }
        .map { (hour, activities) ->
            ActivityData(
                hour = hour,
                activityType = activities.joinToString(", ") { it.activityType },
                caloriesBurned = activities.sumOf { it.caloriesBurned.toDouble() }.toFloat()
            )
        }
        .sortedBy { it.hour }

    // Trasforma i dati raggruppati in entry per il grafico
    val entries = groupedData.map { Entry(it.hour.toFloat(), it.caloriesBurned) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AndroidView(
            factory = { context ->
                LineChart(context).apply {
                    val dataSet = LineDataSet(entries, "Burned Calories").apply {
                        color = Color.RED
                        lineWidth = 2f
                        setCircleColor(Color.RED)
                        valueTextColor = Color.RED
                        setDrawValues(true) // Mostra i valori sul grafico
                    }

                    data = LineData(dataSet)

                    description.isEnabled = false
                    axisLeft.axisMinimum = 0f
                    legend.isEnabled = false

                    // Configurazione asse X
                    xAxis.apply {
                        setGranularity(1f)
                        position = XAxis.XAxisPosition.BOTTOM // Posiziona i valori in basso
                        valueFormatter = object : ValueFormatter() {
                            override fun getFormattedValue(value: Float): String {
                                return "${value.toInt()}h"
                            }
                        }
                    }

                    // Configurazione asse Y
                    axisRight.isEnabled = false // Disabilita l'asse destro
                }
            }, modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        )

        Text(
            text = "Calories Burned",
            style = TextStyle(
                color = androidx.compose.ui.graphics.Color.Black,
                fontSize = 16.sp
            ),
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
