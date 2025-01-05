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

    // Use viewModels() delegate for ViewModel initialization
    private val lastPredictionViewModel: LastPredictionViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        updateRecentPredictions()
    }

    private fun setupObservers() {
        lastPredictionViewModel.lastPredictionData.observe(viewLifecycleOwner) { prediction ->
            try {
                binding.predictionData.text = prediction?.toString() ?: "No prediction available"
                Log.d("DataScreen", "Prediction updated: $prediction")
            } catch (e: Exception) {
                Log.e("DataScreen", "Error updating prediction: ${e.message}")
            }
        }
    }

    private fun updateRecentPredictions() {
        try {
            val lastPredictions = getLastPredictionsFromSharedPreferences(requireContext(), "predictions")
                .take(5)
/*
            val lastPredictions = listOf(
                "Prediction 1",
                "Prediction 2",
                "Prediction 3",
                "Prediction 4",
                "Prediction 5"
            ).take(5)
*/

            binding.recentActivitiesData.text = lastPredictions.joinToString("\n") { it.toString() }
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