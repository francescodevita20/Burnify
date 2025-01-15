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

        // Observe last prediction
        lastPredictionViewModel.lastPredictionData.observe(viewLifecycleOwner) { prediction ->
            Log.d("DataScreen", "Prediction updated: $prediction")
            binding.predictionData.text = when {
                prediction != null -> prediction // Assuming prediction is now a String
                else -> "No prediction available"
            }
        }

        // Observe recent predictions
        lastPredictionViewModel.recentPredictions.observe(viewLifecycleOwner) { predictions ->
            Log.d("DataScreen", "Recent predictions updated: $predictions")
            binding.recentActivitiesData.text = if (predictions.isNotEmpty()) {
                predictions.joinToString("\n") // Predictions are now strings, no need for .toString() on each
            } else {
                "No predictions available"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}