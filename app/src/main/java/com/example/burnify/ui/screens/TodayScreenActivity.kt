package com.example.burnify.ui.screens

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import android.util.Log
import com.example.burnify.databinding.TodayScreenBinding
import com.example.burnify.processor.CaloriesDataProcessor
import com.example.burnify.ui.components.ActivityData
import com.example.burnify.ui.components.HistogramActivityChart
import com.example.burnify.ui.components.CaloriesBurnedChart
import com.example.burnify.viewmodel.PredictedActivityViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import com.example.burnify.util.getSharedPreferences
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TodayScreenActivity : Fragment() {
    private var binding: TodayScreenBinding? = null

    private lateinit var viewModel: PredictedActivityViewModel

    private val caloriesData = mutableStateOf<List<ActivityData>>(emptyList())
    private val classes = mutableStateOf<List<String>>(emptyList())
    private val durations = mutableStateOf<Map<String, Double>>(emptyMap())  // Store duration for each class
    private val weightString by lazy {
        (getSharedPreferences(requireContext(), "userdata", "user_data_key")?.get("weight")).toString()
    }
    private val weight by lazy { weightString.toFloatOrNull()?.toInt() ?: 70 }

    private val handler = Handler(Looper.getMainLooper())
    private val updateInterval = 10000L // 10 seconds

    private val runnable = object : Runnable {
        override fun run() {
            Log.d("HandlerRunnable", "Refreshing data at ${System.currentTimeMillis()}")
            viewModel.loadPredictedActivityData()  // Refresh the data
            handler.postDelayed(this, updateInterval)  // Schedule next refresh
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = TodayScreenBinding.inflate(inflater, container, false)
        // Initialize ViewModel
        viewModel = ViewModelProvider(requireActivity())[PredictedActivityViewModel::class.java]
        // Load data initially
        viewModel.loadPredictedActivityData()

        // Setup Compose views
        setupComposeViews()
        // Observe data changes
        observeData()

        // Start periodic data refreshing
        handler.post(runnable)

        return binding?.root
    }

    private fun setupComposeViews() {
        binding?.histogramChartCompose?.setContent {
            val currentDurations by durations
            // Log the current durations before passing them to the chart
            Log.d("setupComposeViews", "Durations passed to Histogram: $currentDurations")
            HistogramActivityChart(durations = currentDurations)
        }

        binding?.caloriesChartCompose?.setContent {
            val currentCaloriesData by caloriesData
            // Log the current calories data before passing it to the chart
            Log.d("setupComposeViews", "CaloriesData passed to CaloriesBurnedChart: $currentCaloriesData")
            CaloriesBurnedChart(activityData = currentCaloriesData)
        }
    }

    private fun observeData() {
        // Use lifecycleScope to collect flow from ViewModel
        lifecycleScope.launch {
            viewModel.predictedActivityData.collect { data ->
                data?.let {
                    // Logging the entire data to see what we're working with
                    Log.d("observeData", "Data received: $it")

                    // Log the classes and their updates
                    Log.d("observeData", "Classes updated: ${it.map { item -> item.label }}")

                    // Map the activity labels
                    classes.value = it.map { item -> item.label }

                    // Define the timestamp format allowing up to 3 fractional seconds
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")

                    // Map data for calories
                    caloriesData.value = it.map { item ->
                        val processedAt = preprocessTimestamp(item.processedAt)
                        val hour = try {
                            val dateTime = LocalDateTime.parse(processedAt, formatter)
                            dateTime.hour
                        } catch (e: Exception) {
                            Log.e("observeData", "Error parsing timestamp: ${item.processedAt}")
                            0  // Default value in case of error
                        }

                        val activityData = ActivityData(
                            hour = hour,
                            activityType = item.label,
                            caloriesBurned = CaloriesDataProcessor.processMeasurements(
                                weight,
                                item.label,
                                0.00555556f
                            )
                        )
                        Log.d("observeData", "ActivityData processed: $activityData")
                        activityData
                    }

                    // Log the durations map
                    val durationsMap = it.fold(mutableMapOf<String, Double>()) { acc, item ->
                        val label = item.label ?: ""
                        acc[label] = (acc[label] ?: 0.0) + item.durationMinutes
                        acc
                    }
                    durations.value = durationsMap
                    Log.d("observeData", "Durations updated: $durationsMap")

                    // Update the total calories after the data is processed
                    updateTotalCalories()
                }
            }
        }
    }

    // Function to preprocess timestamp and ensure milliseconds are 3 digits
    private fun preprocessTimestamp(timestamp: String): String {
        val regex = "(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2})(\\.\\d{1,3})?".toRegex()

        // Check if the timestamp matches the expected pattern
        val matchResult = regex.find(timestamp)

        return if (matchResult != null) {
            // Extract the timestamp and milliseconds part (if any)
            val baseTime = matchResult.groupValues[1]
            val milliseconds = matchResult.groupValues[2]

            // If milliseconds are present, ensure they have 3 digits; if not, add ".000"
            val formattedMilliseconds = if (milliseconds.isNotEmpty()) {
                // Pad milliseconds to exactly 3 digits
                val ms = milliseconds.substring(1).padEnd(3, '0')  // Remove '.' and pad to 3 digits
                ".$ms"
            } else {
                ".000"
            }

            // Combine base time with formatted milliseconds
            "$baseTime$formattedMilliseconds"
        } else {
            // If timestamp does not match the expected format, return the original timestamp
            timestamp
        }
    }

    private fun updateTotalCalories() {
        // Ensure caloriesData is not null and contains values
        val total = caloriesData.value?.sumOf { it.caloriesBurned.toDouble() } ?: 0.0

        // Log the total calories being calculated
        Log.d("updateTotalCalories", "Total Calories Burned calculated: %.2f kcal".format(total))

        // Update the TextView on the main thread
        activity?.runOnUiThread {
            binding?.totalCaloriesText?.visibility = View.VISIBLE
            binding?.totalCaloriesText?.text = "Total Calories Burned: %.2f kcal".format(total)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Stop the handler when the view is destroyed
        handler.removeCallbacks(runnable)
        binding = null
    }
}
