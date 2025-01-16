package com.example.burnify.ui.screens

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.burnify.databinding.DataScreenBinding
import com.example.burnify.viewmodel.LastPredictionViewModel
import java.text.SimpleDateFormat
import java.util.*

class DataScreen : Fragment() {

    private var _binding: DataScreenBinding? = null
    private val binding get() = _binding!!

    private val lastPredictionViewModel: LastPredictionViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = DataScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("DataScreen", "onViewCreated called")

        // Set up observers
        setupObservers()
    }

    private fun setupObservers() {
        Log.d("DataScreen", "Setting up observers")

        // Observe the most recent prediction
        lastPredictionViewModel.lastPredictionData.observe(viewLifecycleOwner) { prediction ->
            Log.d("DataScreen", "Prediction updated: $prediction")
            binding.predictionData.text = when {
                prediction != null -> prediction // prediction is a String (label)
                else -> "No prediction available"
            }
        }

        // Observe the list of recent predictions
        lastPredictionViewModel.recentPredictions.observe(viewLifecycleOwner) { predictions ->
            Log.d("DataScreen", "Recent predictions updated: $predictions")
            binding.recentActivitiesData.text = if (predictions.isNotEmpty()) {
                predictions.joinToString("\n") { prediction ->
                    // Format timestamp to a readable string
                    val timestamp = prediction.timestamp.toLong()
                    val formattedTime = formatTimestamp(timestamp)
                    "$formattedTime: ${prediction.label}"
                }
            } else {
                "No predictions available"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Helper function to format timestamp to a readable date format
    private fun formatTimestamp(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date = Date(timestamp)
        return dateFormat.format(date)
    }
}
