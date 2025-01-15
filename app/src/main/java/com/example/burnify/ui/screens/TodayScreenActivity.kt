package com.example.burnify.ui.screens
import android.os.Bundle
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
import com.example.burnify.util.getSharedPreferences


class TodayScreenActivity : Fragment() {
    private var binding: TodayScreenBinding? = null

    private lateinit var viewModel: PredictedActivityViewModel

    private val caloriesData = mutableStateOf<List<ActivityData>>(emptyList())
    private val classes = mutableStateOf<List<String>>(emptyList())
    private val weightString by lazy {
        (getSharedPreferences(requireContext(), "userdata", "user_data_key")?.get("weight")).toString()
    }
    private val weight by lazy { weightString.toFloatOrNull()?.toInt() ?: 70 }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = TodayScreenBinding.inflate(inflater, container, false)
        // Initialize ViewModel
        viewModel = ViewModelProvider(requireActivity())[PredictedActivityViewModel::class.java]
        // Load data
        viewModel.loadPredictedActivityData()
        // Setup Compose views
        setupComposeViews()
        // Observe data changes
        observeData()

        return binding?.root
    }

    private fun setupComposeViews() {
        binding?.histogramChartCompose?.setContent {
            val currentClasses by classes
            HistogramActivityChart(classes = currentClasses)
        }

        binding?.caloriesChartCompose?.setContent {
            val currentCaloriesData by caloriesData
            CaloriesBurnedChart(activityData = currentCaloriesData)
        }
    }

    private fun observeData() {
        viewModel.predictedActivityData.observe(viewLifecycleOwner) { data ->
            data?.let {
                // Logging to verify the data is being received
                Log.d("observeData", "Data received: $it")

                classes.value = it.map { item -> item.label.toString() }
                caloriesData.value = it.map { item ->
                    ActivityData(
                        hour = item.processedAt.toInt(),
                        activityType = item.label.toString(),
                        caloriesBurned = CaloriesDataProcessor.processMeasurements(
                            weight,
                            item.label.toString(),
                            0.00555556f
                        )
                    )
                }

                // Update the total calories after the data is processed
                updateTotalCalories()
            }
        }
    }

    private fun updateTotalCalories() {
        // Ensure caloriesData is not null and contains values
        val total = caloriesData.value?.sumOf { it.caloriesBurned.toDouble() } ?: 0.0

        // Update the TextView on the main thread
        activity?.runOnUiThread {
            // Ensure TextView is visible
            binding?.totalCaloriesText?.visibility = View.VISIBLE
            binding?.totalCaloriesText?.text = "Total Calories Burned: %.2f kcal".format(total)
        }

        // Debugging output in the log
        Log.d("updateTotalCalories", "Total Calories Burned: %.2f kcal".format(total))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

}
