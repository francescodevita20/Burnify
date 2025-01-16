package com.example.burnify.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.burnify.util.addPredictionToSharedPreferences
import com.example.burnify.util.getLastPredictionsFromSharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Data class for prediction with timestamp and label
data class Prediction(
    val timestamp: String,
    val label: String
)

class LastPredictionViewModel(application: Application) : AndroidViewModel(application) {

    private val _lastPredictionData = MutableLiveData<String?>()
    val lastPredictionData: LiveData<String?> = _lastPredictionData

    private val _recentPredictions = MutableLiveData<List<Prediction>>()
    val recentPredictions: LiveData<List<Prediction>> = _recentPredictions

    init {
        // Initialize with null
        _lastPredictionData.value = null
        // Load initial recent predictions
        loadRecentPredictions()
    }

    private fun loadRecentPredictions() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val context = getApplication<Application>().applicationContext
                val rawPredictions = getLastPredictionsFromSharedPreferences(context, "predictions")

                // No need to split, just use the list of Prediction objects directly
                _recentPredictions.postValue(rawPredictions)
            } catch (e: Exception) {
                Log.e("LastPredictionViewModel", "Error loading recent predictions: ${e.message}")
                _recentPredictions.postValue(emptyList())
            }
        }
    }

    fun updateLastPredictionData(lastPrediction: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val context = getApplication<Application>().applicationContext

                // Generate a timestamp
                val timestamp = System.currentTimeMillis().toString() // You can format for better readability
                val prediction = Prediction(timestamp = timestamp, label = lastPrediction)

                val saveSuccess = addPredictionToSharedPreferences(context, prediction, "predictions")
                if (saveSuccess) {
                    Log.d("LastPredictionViewModel", "Updating prediction data with: $prediction")
                    _lastPredictionData.postValue(lastPrediction)
                    loadRecentPredictions()
                } else {
                    Log.e("LastPredictionViewModel", "Failed to save prediction")
                }
            } catch (e: Exception) {
                Log.e("LastPredictionViewModel", "Error updating prediction: ${e.message}")
            }
        }
    }

    // Filter predictions to remove continuous identical labels
    fun filterNonContinuousPredictions(predictions: List<Prediction>): List<Prediction> {
        if (predictions.isEmpty()) return emptyList()

        val filtered = mutableListOf<Prediction>()
        filtered.add(predictions[0]) // Always add the first prediction

        for (i in 1 until predictions.size) {
            if (predictions[i].label != predictions[i - 1].label) {
                filtered.add(predictions[i])
            }
        }
        return filtered
    }
}
