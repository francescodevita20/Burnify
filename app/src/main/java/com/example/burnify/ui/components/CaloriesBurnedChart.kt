package com.example.burnify.ui.components

import android.graphics.Color
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.components.XAxis

// Data model to represent activities
data class ActivityData(
    val hour: Int,            // Hour of the day (0-23)
    val activityType: String, // Type of activity (e.g., "running", "walking")
    val caloriesBurned: Float // Calories burned during that hour
)

@Composable
fun CaloriesBurnedChart(activityData: List<ActivityData>) {

    // Filter for valid hours (0-23)
    val validData = activityData.filter { it.hour in 0..23 }

    // Log filtered valid data
    LaunchedEffect(validData) {
        println("Filtered valid data: $validData")
    }

    // Group data by hour and sum up the calories burned
    val groupedData = validData
        .groupBy { it.hour } // Group data by hour
        .map { (hour, activities) ->
            ActivityData(
                hour = hour,
                activityType = activities.joinToString(", ") { it.activityType }, // Concatenate activity types for the same hour
                caloriesBurned = activities.fold(0f) { sum, activity -> sum + activity.caloriesBurned } // Sum up calories burned
            )
        }
        .sortedBy { it.hour } // Sort by hour

    // Log the grouped data for debugging purposes
    LaunchedEffect(groupedData) {
        println("Grouped data passed to chart: $groupedData")
    }

    // Convert the grouped data into chart entries
    val entries = groupedData.map { Entry(it.hour.toFloat(), it.caloriesBurned) }

    // If there are no entries, log and return early
    if (entries.isEmpty()) {
        println("No data to display in the chart.")
        return
    }

    // Log the entries being used for the chart
    LaunchedEffect(entries) {
        println("Chart entries: $entries")
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Display the chart using AndroidView to embed a LineChart from MPAndroidChart
        AndroidView(
            factory = { context ->
                LineChart(context).apply {
                    val dataSet = LineDataSet(entries, "Burned Calories").apply {
                        color = Color.RED // Set line color
                        lineWidth = 2f // Set line width
                        valueTextSize = 12f // Increase this value to make the text bigger
                        setCircleColor(Color.RED) // Set circle color at each data point
                        valueTextColor = Color.BLUE // Set text color for the calorie values
                        setDrawValues(true) // Display values on the chart
                    }

                    // Set the data for the chart
                    data = LineData(dataSet)

                    // Force a refresh of the chart
                    invalidate()

                    // Disable the description (the label on the top of the chart)
                    description.isEnabled = false

                    // Set the minimum value for the Y-axis
                    axisLeft.axisMinimum = 0f

                    // Disable the legend
                    legend.isEnabled = false

                    // Configure the X-axis
                    xAxis.apply {
                        setGranularity(1f) // Allow only whole numbers on the X-axis
                        position = XAxis.XAxisPosition.BOTTOM // Position the X-axis labels at the bottom
                        valueFormatter = object : ValueFormatter() {
                            // Format the X-axis labels as hours
                            override fun getFormattedValue(value: Float): String {
                                return "${value.toInt()}h"
                            }
                        }
                    }

                    // Disable the right Y-axis (we don't need it in this case)
                    axisRight.isEnabled = false
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        )

        // Display a label for the chart
        Text(
            text = "Calories Burned",
            style = TextStyle(
                color = androidx.compose.ui.graphics.Color.Black,
                fontSize = 16.sp
            ),
            modifier = Modifier.padding(top = 8.dp) // Padding above the label
        )
    }
}
