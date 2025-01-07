package com.example.burnify.ui.screens

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.burnify.databinding.DataScreenBinding
import com.example.burnify.viewmodel.LastPredictionViewModel
import com.example.burnify.util.getLastPredictionsFromSharedPreferences

class DataScreen : Fragment() {

    private var _binding: DataScreenBinding? = null
    private val binding get() = _binding!!

    // ViewModel initialization using the 'viewModels()' delegate
    private val lastPredictionViewModel: LastPredictionViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = DataScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("DataScreen", "onViewCreated called")
        Log.d("DataScreen", "Fragment lifecycle state: ${viewLifecycleOwner.lifecycle.currentState}")

        // Set up observers
        setupObservers()

        // Update recent predictions data from SharedPreferences
        updateRecentPredictions()
    }

    private fun setupObservers() {
        Log.d("DataScreen", "Setting up observers")

        // Observe LiveData changes in the ViewModel using the viewLifecycleOwner to avoid issues after the view is destroyed
        lastPredictionViewModel.lastPredictionData.observe(viewLifecycleOwner, Observer { prediction ->
            Log.d("DataScreen", "Prediction updated: $prediction")

            // Update the UI with the new prediction, handle null or unexpected values
            try {
                binding.predictionData.text = prediction?.toString() ?: "No prediction available"
            } catch (e: Exception) {
                Log.e("DataScreen", "Error updating prediction: ${e.message}")
                binding.predictionData.text = "Error updating prediction"
            }
        })
    }

    private fun updateRecentPredictions() {
        try {
            // Retrieve the last 5 predictions from SharedPreferences
            val lastPredictions = getLastPredictionsFromSharedPreferences(requireContext(), "predictions")

            // Update the UI with recent predictions
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
        _binding = null  // Nullify the binding to avoid memory leaks when the view is destroyed
    }
}
