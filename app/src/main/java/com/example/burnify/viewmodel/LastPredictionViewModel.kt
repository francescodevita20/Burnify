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

class LastPredictionViewModel(application: Application) : AndroidViewModel(application) {
    private val _lastPredictionData = MutableLiveData<Int?>()
    val lastPredictionData: MutableLiveData<Int?> = _lastPredictionData

    private val _recentPredictions = MutableLiveData<List<Int>>()
    val recentPredictions: LiveData<List<Int>> = _recentPredictions

    init {
        // Initialize with null
        _lastPredictionData.value = null
        // Load initial recent predictions
        loadRecentPredictions()
    }

    private fun loadRecentPredictions() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val predictions = getLastPredictionsFromSharedPreferences(
                    getApplication<Application>().applicationContext,
                    "predictions"
                )
                _recentPredictions.postValue(predictions)
            } catch (e: Exception) {
                Log.e("LastPredictionViewModel", "Error loading recent predictions: ${e.message}")
                _recentPredictions.postValue(emptyList())
            }
        }
    }

    fun updateLastPredictionData(lastPrediction: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val context = getApplication<Application>().applicationContext
                val saveSuccess = addPredictionToSharedPreferences(context, lastPrediction, "predictions")
                if (saveSuccess) {
                    Log.d("LastPredictionViewModel", "Updating prediction data with: $lastPrediction")
                    _lastPredictionData.postValue(lastPrediction)
                    // Reload recent predictions after adding new one
                    loadRecentPredictions()
                    Log.d("LastPredictionViewModel", "LiveData updated with: $lastPrediction")
                } else {
                    Log.e("LastPredictionViewModel", "Failed to save prediction")
                }
            } catch (e: Exception) {
                Log.e("LastPredictionViewModel", "Error updating prediction: ${e.message}")
            }
        }
    }
}