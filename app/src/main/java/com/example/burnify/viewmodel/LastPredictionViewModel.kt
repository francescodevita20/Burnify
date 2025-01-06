package com.example.burnify.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.burnify.util.addPredictionToSharedPreferences

class LastPredictionViewModel(application: Application) : AndroidViewModel(application) {


    private val _lastPredictionData = MutableLiveData<Int>() // Singolo campione

    val lastPredictionData : LiveData<Int> get() = _lastPredictionData

    fun updateLastPredictionData(lastPrediction: Int){
        _lastPredictionData.postValue(lastPrediction)

        // Call the utility function to store the prediction in SharedPreferences
        val context = getApplication<Application>().applicationContext
        val sharedPreferencesName = "last_predictions"

        addPredictionToSharedPreferences(context, lastPrediction, sharedPreferencesName)

        println("Last Prediction: $lastPrediction")
    }


}
