package com.example.burnify.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.burnify.database.AppDatabaseProvider
import com.example.burnify.database.ActivityPrediction
import com.example.burnify.dao.ActivityPredictionDao
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder

class PredictedActivityViewModel(application: Application) : AndroidViewModel(application) {

    // LiveData per una lista di ActivityPrediction
    private val _predictedActivityData = MutableLiveData<List<ActivityPrediction>>()
    val predictedActivityData: LiveData<List<ActivityPrediction>> get() = _predictedActivityData

    private val activityPredictionDao: ActivityPredictionDao =
        AppDatabaseProvider.getInstance(application).activityPredictionDao()

    // Funzione per caricare i dati dal database
    fun loadPredictedActivityData() {
        viewModelScope.launch {
            try {
                val predictions =
                    activityPredictionDao.getSamplesFromLastDay()  // Recupera i dati dal DAO

                // Modifica la stringa 'processedAt' per contenere solo l'ora
                val formattedPredictions = predictions.map {
                    it.copy(processedAt = getHourFromDate(it.processedAt).toString())
                }

                // Imposta la lista di dati formattati
                _predictedActivityData.postValue(formattedPredictions)
            } catch (e: Exception) {
                e.printStackTrace()
                // Logga o gestisci l'errore in caso di problemi con il caricamento dei dati
            }
        }
    }

    // Funzione per aggiornare i dati (se necessario)
    fun updatePredictedActivityData(predictions: List<ActivityPrediction>) {
        _predictedActivityData.postValue(predictions)
    }

    // Funzione per estrarre solo l'ora dalla data
    fun getHourFromDate(dateString: String): Int {

        return try {
            // Normalizza la stringa: aggiunge una cifra mancante nei millisecondi se necessario
            val normalizedDateString = if (dateString.contains(".")) {
                val parts = dateString.split(".")
                if (parts[1].length < 3) {
                    parts[0] + "." + parts[1].padEnd(3, '0') // Aggiunge zeri per arrivare a 3 cifre
                } else {
                    dateString
                }
            } else {
                dateString
            }

            // Formatter per il parsing
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
            val dateTime = LocalDateTime.parse(normalizedDateString, formatter)

            // Restituisce solo l'ora come intero
            dateTime.hour
        } catch (e: Exception) {
            e.printStackTrace()
            -1 // Valore di fallback in caso di errore
        }
    }


}

