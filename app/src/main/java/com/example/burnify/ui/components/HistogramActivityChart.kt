package com.example.burnify.ui.components

import android.graphics.Color
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color as ComposeColor
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.formatter.ValueFormatter
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun HistogramActivityChart(durations: Map<String, Double>) {

    // Log the received durations to ensure we get the correct values
    LaunchedEffect(durations) {
        Log.d("HistogramActivityChart", "Received durations: $durations")
    }

    // If there are no valid durations or if all are zeros, show the loading message
    if (durations.isEmpty() || durations.values.all { it == 0.0 }) {
        Log.d("HistogramActivityChart", "No valid durations, showing loading message.")
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Activity Chart is loading...",
                style = TextStyle(
                    color = ComposeColor.Black,
                    fontSize = 16.sp
                ),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    } else {
        // Generate the list of BarEntry objects for the bar chart
        val barEntries = durations.entries.mapIndexed { index, entry ->
            Log.d("HistogramActivityChart", "Creating bar entry for ${entry.key}: ${entry.value}")
            BarEntry(index.toFloat(), entry.value.toFloat())
        }

        // Calculate the maximum duration for setting Y-axis range
        val maxDuration = durations.values.maxOrNull()?.toFloat() ?: 0f
        Log.d("HistogramActivityChart", "Max duration: $maxDuration")

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Render the bar chart using AndroidView
            AndroidView(
                factory = { context ->
                    BarChart(context).apply {
                        // Disable the chart description
                        description.isEnabled = false

                        // Configure the left Y-axis to show integer values
                        axisLeft.valueFormatter = object : ValueFormatter() {
                            override fun getFormattedValue(value: Float): String {
                                return value.toInt().toString()
                            }
                        }
                        axisLeft.axisMinimum = 0f
                        axisLeft.axisMaximum = maxDuration * 1.1f // Add a margin to the max Y value
                        axisLeft.setLabelCount(3, true)

                        // Configure the X-axis to show class names
                        xAxis.setGranularity(1f) // Set the granularity of the X-axis
                        xAxis.valueFormatter = object : ValueFormatter() {
                            override fun getFormattedValue(value: Float): String {
                                return durations.keys.elementAtOrNull(value.toInt()) ?: ""
                            }
                        }

                        // Disable the right Y-axis
                        axisRight.isEnabled = false

                        // Disable the legend
                        legend.isEnabled = false

                        // Disable markers (text labels above bars)
                        setDrawMarkers(false)

                        // Position the X-axis labels at the bottom to prevent overlap
                        xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                    }
                },
                modifier = Modifier.fillMaxWidth().height(300.dp), // Set the chart height
                // Use a key to force recreation when `durations` change
                update = { barChart ->
                    // Clear previous data before setting new data
                    barChart.clear()

                    // Create a new BarDataSet with the current data
                    val dataSet = BarDataSet(barEntries, "Activity Durations").apply {
                        setColor(Color.parseColor("#B0BEC5"))
                        valueTextSize = 12f // Adjust the text size on the bars
                    }

                    // Set the new BarData to the chart
                    barChart.data = BarData(dataSet)

                    // Force a layout pass and redraw after setting new data
                    barChart.notifyDataSetChanged()
                    barChart.invalidate()

                    // Log chart data setting
                    Log.d("HistogramActivityChart", "Chart data set with ${barEntries.size} entries.")
                }
            )
        }
    }
}
