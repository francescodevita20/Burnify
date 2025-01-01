package com.example.burnify.ui.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.burnify.databinding.DataScreenBinding
import com.example.burnify.viewmodel.AccelerometerViewModel
import com.example.burnify.viewmodel.GyroscopeViewModel
import com.example.burnify.viewmodel.MagnetometerViewModel
import com.example.burnify.viewmodel.PredictedActivityViewModel
import com.example.burnify.viewmodel.LastPredictionViewModel
import com.example.burnify.util.getLastPredictionsFromSharedPreferences

class DataScreen : Fragment() {

    private var _binding: DataScreenBinding? = null
    private val binding get() = _binding!!

    // ViewModels for the sensors and predicted activity
    private val accelerometerViewModel: AccelerometerViewModel by viewModels()
    private val gyroscopeViewModel: GyroscopeViewModel by viewModels()
    private val magnetometerViewModel: MagnetometerViewModel by viewModels()
    private val predictedActivityViewModel: PredictedActivityViewModel by viewModels()
    private val lastPredictionViewModel: LastPredictionViewModel by viewModels()

    // Function to update UI based on data from ViewModels
    private fun updateUI() {
        // Observe the data from ViewModels
        accelerometerViewModel.accelerometerData.observe(viewLifecycleOwner, Observer { data ->
            binding.accelerometerData.text = "Samples: ${data?.getSamples()?.size ?: 0}"
        })

        gyroscopeViewModel.gyroscopeData.observe(viewLifecycleOwner, Observer { data ->
            binding.gyroscopeData.text = "Samples: ${data?.getSamples()?.size ?: 0}"
        })

        magnetometerViewModel.magnetometerData.observe(viewLifecycleOwner, Observer { data ->
            binding.magnetometerData.text = "Samples: ${data?.getSamples()?.size ?: 0}"
        })

        lastPredictionViewModel.lastPredictionData.observe(viewLifecycleOwner, Observer { prediction ->
            binding.predictionData.text = prediction?.toString() ?: "No prediction available."
        })

        // Retrieve recent predictions from shared preferences
        val lastPredictions = getLastPredictionsFromSharedPreferences(requireContext(), "predictions").take(5)
        binding.recentActivitiesData.text = lastPredictions.joinToString("\n") { it.toString() }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DataScreenBinding.inflate(inflater, container, false)

        // Update UI with ViewModel data
        updateUI()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
