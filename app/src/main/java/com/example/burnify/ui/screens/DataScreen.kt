// DataScreen.kt
package com.example.burnify.ui.screens

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels  // Changed from viewModels
import androidx.lifecycle.Observer
import com.example.burnify.databinding.DataScreenBinding
import com.example.burnify.viewmodel.LastPredictionViewModel
import com.example.burnify.util.getLastPredictionsFromSharedPreferences

class DataScreen : Fragment() {

    private var _binding: DataScreenBinding? = null
    private val binding get() = _binding!!

    // Changed to activityViewModels() to share ViewModel across fragments
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

        // Set initial value
        updateInitialPrediction()

        // Set up observers
        setupObservers()

        // Update recent predictions data from SharedPreferences
        updateRecentPredictions()
    }

    private fun updateInitialPrediction() {
        // Get the last prediction from SharedPreferences and set it
        try {
            val lastPredictions = getLastPredictionsFromSharedPreferences(requireContext(), "predictions")
            if (lastPredictions.isNotEmpty()) {
                lastPredictionViewModel.updateLastPredictionData(lastPredictions.first())
            }
        } catch (e: Exception) {
            Log.e("DataScreen", "Error setting initial prediction: ${e.message}")
        }
    }

    private fun setupObservers() {
        Log.d("DataScreen", "Setting up observers")

        // Remove the Observer type to let Kotlin infer it
        lastPredictionViewModel.lastPredictionData.observe(viewLifecycleOwner) { prediction ->
            Log.d("DataScreen", "Prediction updated: $prediction")

            binding.predictionData.text = when {
                prediction != null -> prediction.toString()
                else -> "No prediction available"
            }
        }
    }

    private fun updateRecentPredictions() {
        try {
            val lastPredictions = getLastPredictionsFromSharedPreferences(requireContext(), "predictions")
            binding.recentActivitiesData.text = if (lastPredictions.isNotEmpty()) {
                lastPredictions.joinToString("\n")
            } else {
                "No predictions available"
            }
            Log.d("DataScreen", "Recent predictions updated: $lastPredictions")
        } catch (e: Exception) {
            Log.e("DataScreen", "Error updating recent predictions: ${e.message}")
            binding.recentActivitiesData.text = "Unable to load recent predictions"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}