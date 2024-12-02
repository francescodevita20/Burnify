package com.example.burnify.ui.components

import android.graphics.Color
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
fun HistogramActivityChart(classes: List<String>) {

    // Log the received class data for debugging purposes
    LaunchedEffect(classes) {
        println("Received classes: $classes")
    }

    // Display loading message if no classes are provided
    if (classes.isEmpty()) {
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
        // Define the possible 12 classes (1 to 12)
        val allClasses = (1..12).map { it.toString() }

        // Count occurrences of each class in the provided 'classes' list
        val classOccurrences = allClasses.associateWith { className ->
            classes.count { it == className }
        }

        // Log the class occurrences for debugging purposes
        println("Class occurrences: $classOccurrences")

        // Sort the classes in order from 1 to 12
        val sortedClasses = allClasses

        // Create a list of BarEntry objects from the class occurrences
        val barEntries = sortedClasses.mapIndexed { index, className ->
            BarEntry(index.toFloat(), classOccurrences[className]?.toFloat() ?: 0f)
        }

        // Calculate the maximum occurrence value to set axis range
        val maxOccurrences = classOccurrences.values.maxOrNull()?.toFloat() ?: 0f

        // Create y-axis values: 0, max/2, max
        val yValues = listOf(0f, maxOccurrences / 2, maxOccurrences)

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Render the bar chart using AndroidView to embed a BarChart from MPAndroidChart
            AndroidView(factory = { context ->
                BarChart(context).apply {
                    // Create a dataset for the bar chart
                    val dataSet = BarDataSet(barEntries, "Class Occurrences").apply {
                        // Set a light gray color for all bars
                        setColor(Color.parseColor("#B0BEC5"))
                    }

                    // Set the data for the chart
                    data = BarData(dataSet)

                    // Disable the chart description
                    description.isEnabled = false

                    // Custom y-axis value formatter: Display integer values only
                    axisLeft.valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return value.toInt().toString() // Convert float to integer for display
                        }
                    }

                    // Set the granularity and range for the left y-axis
                    axisLeft.setGranularity(1f)
                    axisLeft.axisMinimum = 0f
                    axisLeft.axisMaximum = maxOccurrences * 1.1f // Add a margin to the max y-value
                    axisLeft.setGranularityEnabled(true) // Enable granularity to avoid small intervals

                    // Add labels at 0, max/2, and max values for clarity
                    axisLeft.setLabelCount(3, true)
                    axisLeft.valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return when (value) {
                                0f -> "0"
                                maxOccurrences / 2 -> "${(maxOccurrences / 2).toInt()}"
                                maxOccurrences -> "${maxOccurrences.toInt()}"
                                else -> ""
                            }
                        }
                    }

                    // Set x-axis granularity (spacing between labels)
                    xAxis.setGranularity(1f)

                    // Custom x-axis value formatter to display class names from 1 to 12
                    xAxis.valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return sortedClasses.getOrElse(value.toInt()) { "" }
                        }
                    }

                    // Disable the right y-axis (not needed in this case)
                    axisRight.isEnabled = false

                    // Disable the legend below the chart
                    legend.isEnabled = false

                    // Remove markers (text labels above bars)
                    setDrawMarkers(false)

                    // Position the x-axis labels at the bottom to prevent overlap
                    xAxis.setPosition(com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM)

                    // Set axis limits and margin for clarity
                    axisLeft.axisMinimum = 0f
                    axisLeft.axisMaximum = (maxOccurrences * 1.1f).toFloat()
                }
            }, modifier = Modifier
                .fillMaxWidth()
                .height(300.dp) // Set chart height
            )

            // Title under the chart
            Text(
                text = "Activity History",
                style = TextStyle(
                    color = ComposeColor.Black,
                    fontSize = 16.sp
                ),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
