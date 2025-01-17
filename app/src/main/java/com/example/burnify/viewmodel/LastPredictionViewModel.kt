package com.example.burnify.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.burnify.util.addPredictionToSharedPreferences
import com.example.burnify.util.getTodayPredictionsFromSharedPreferences
import com.example.burnify.util.clearSharedPreferencesForNewDay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

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

                // Step 1: Get all the predictions stored in SharedPreferences for today
                val rawPredictions = getTodayPredictionsFromSharedPreferences(context, "predictions")

                // Step 2: Check if the last prediction's date is different from today
                val lastPrediction = rawPredictions.lastOrNull()
                val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                if (lastPrediction != null) {
                    // If lastPrediction is not null, check its date
                    val lastPredictionDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(lastPrediction.timestamp.toLong()))

                    if (lastPredictionDate != currentDate) {
                        // A) If the last prediction's date is not today, clear SharedPreferences and save the current prediction
                        clearSharedPreferencesForNewDay(context, "predictions")

                        // Update recent predictions with an empty list
                        _recentPredictions.postValue(emptyList())
                    } else {
                        // B) If the last prediction is from today, just update the recent predictions with the list from SharedPreferences
                        _recentPredictions.postValue(rawPredictions)
                    }
                } else {
                    // If no predictions are available, pass a empty list
                    _recentPredictions.postValue(rawPredictions)
                }

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
}
