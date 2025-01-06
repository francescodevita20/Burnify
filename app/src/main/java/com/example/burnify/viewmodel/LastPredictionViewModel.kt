package com.example.burnify.viewmodel

import android.app.Application
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.burnify.util.addPredictionToSharedPreferences


class LastPredictionViewModel(application: Application) : AndroidViewModel(application) {

    private val _lastPredictionData = MutableLiveData<Int>() // Singolo campione

    val lastPredictionData : LiveData<Int> get() = _lastPredictionData
    fun updateLastPredictionData(lastPrediction: Int){
        val context = getApplication<Application>().applicationContext
        val lastPredictionsName="last_predictions"
        addPredictionToSharedPreferences(context, lastPrediction, lastPredictionsName)
        _lastPredictionData.postValue(lastPrediction)
        Log.d("LastPredictionViewModel", "Last prediction data updated: $lastPrediction")
        println("Last Prediction: $lastPrediction")
    }


}