package com.example.burnify.viewmodel

import android.app.Application
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.burnify.util.addPredictionToSharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LastPredictionViewModel(application: Application) : AndroidViewModel(application) {
    private val _lastPredictionData = MutableLiveData<Int>()
    val lastPredictionData: LiveData<Int> = _lastPredictionData

    fun updateLastPredictionData(lastPrediction: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>().applicationContext
            val saveSuccess = addPredictionToSharedPreferences(context, lastPrediction, "predictions")
            if (saveSuccess) {
                Log.d("LastPredictionViewModel", "Updating prediction data with: $lastPrediction")
                _lastPredictionData.postValue(lastPrediction)
                Log.d("LastPredictionViewModel", "LiveData updated with: $lastPrediction")
            }
        }
    }
}