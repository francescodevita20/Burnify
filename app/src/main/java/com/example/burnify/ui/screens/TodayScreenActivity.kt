package com.example.burnify.ui.screens
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.burnify.R
import com.example.burnify.databinding.TodayScreenBinding
import com.example.burnify.processor.CaloriesDataProcessor
import com.example.burnify.ui.components.ActivityData
import com.example.burnify.ui.components.HistogramActivityChart
import com.example.burnify.ui.components.CaloriesBurnedChart
import com.example.burnify.viewmodel.PredictedActivityViewModel
import org.json.JSONObject
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLifecycleOwner

class TodayScreenActivity : Fragment() {
    private lateinit var binding: TodayScreenBinding
    private lateinit var viewModel: PredictedActivityViewModel

    private var classes: List<String> = emptyList()
    private var caloriesData: List<ActivityData> = emptyList()

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

        return binding.root
    }
    private fun setupComposeViews() {
        /*
        Setup Histogram Chart ComposeView
         */
        binding.histogramChartCompose.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides viewLifecycleOwner) {
                HistogramActivityChart(classes = classes)
            }
        }

        /*
        Setup Calories Chart ComposeView
         */
        binding.caloriesChartCompose.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides viewLifecycleOwner) {
                CaloriesBurnedChart(activityData = caloriesData)
            }
        }
    }

    private fun observeData() {
        viewModel.predictedActivityData.observe(viewLifecycleOwner) { activityData ->
            if (activityData != null) {
                // Update classes for histogram
                classes = activityData.map { it.label.toString() }

                // Update calories data
                val weight = getStoredWeight()
                caloriesData = activityData.map {
                    ActivityData(
                        hour = it.processedAt.toInt(),
                        activityType = it.label.toString(),
                        caloriesBurned = CaloriesDataProcessor.processMeasurements(
                            weight,
                            it.label.toString(),
                            0.00555556f
                        )
                    )
                }

                // Update total calories text
                val totalCalories = caloriesData.sumOf { it.caloriesBurned.toDouble() }
                binding.totalCaloriesText.text = "Total Calories Burned: %.2f kcal".format(totalCalories)
            }
        }
    }

    private fun getStoredWeight(): Int {
        val weightString = requireContext().getSharedPreferences("userdata", Context.MODE_PRIVATE)
            .getString("user_data_key", null)
            ?.let { JSONObject(it).optString("weight") }
        return weightString?.toFloatOrNull()?.toInt() ?: 70
    }
}
