package com.example.burnify.viewmodel

import com.example.burnify.util.preprocessTimestamp
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.burnify.database.AppDatabaseProvider
import com.example.burnify.database.dao.ActivityPredictionDao
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class PredictedActivityViewModel(application: Application) : AndroidViewModel(application) {

    // LiveData per una lista di ActivityPrediction con durata inclusa
    private val _predictedActivityData = MutableLiveData<List<ActivityPredictionWithDuration>>()
    val predictedActivityData: LiveData<List<ActivityPredictionWithDuration>> get() = _predictedActivityData

    private val activityPredictionDao: ActivityPredictionDao =
        AppDatabaseProvider.getInstance(application).activityPredictionDao()

    fun loadPredictedActivityData() {
        viewModelScope.launch {
            try {
                val predictions = activityPredictionDao.getSamplesFromLastDay()

                // Calculate the duration for each prediction regardless of the label
                val predictionsWithDurations = predictions.mapIndexed { index, current ->
                    val next = predictions.getOrNull(index + 1)

                    // Preprocess the timestamps before calculating duration
                    val processedAt = preprocessTimestamp(current.processedAt)
                    val nextProcessedAt = next?.let { preprocessTimestamp(it.processedAt) }

                    // Calculate duration between current and next prediction
                    val durationSeconds = if (nextProcessedAt != null) {
                        calculateDuration(processedAt, nextProcessedAt)
                    } else {
                        0.0 // If there is no next prediction, set duration as 0
                    }

                    // If the duration exceeds 10 seconds, set it to 10 seconds
                    val adjustedDuration = if (durationSeconds > 10) {
                        10.0
                    } else {
                        durationSeconds
                    }

                    // Format duration to 1 decimal place
                    val formattedDuration = String.format(Locale.US, "%.1f", adjustedDuration / 60.0)  // Convert seconds to minutes

                    // Append duration to label
                    ActivityPredictionWithDuration(
                        id = current.id,
                        processedAt = processedAt,
                        label = current.label ?: "", // Provide default empty string if label is null
                        durationMinutes = adjustedDuration / 60.0 // Store in minutes
                    )
                }

                // Post updated predictions with durations to LiveData
                _predictedActivityData.postValue(predictionsWithDurations)
            } catch (e: Exception) {
                e.printStackTrace()
                // Handle error (maybe log it or show a message)
            }
        }
    }

    private fun calculateDuration(startTime: String, endTime: String): Double {
        return try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
            val start = LocalDateTime.parse(startTime, formatter)
            val end = LocalDateTime.parse(endTime, formatter)

            // Calculate duration in seconds
            val durationSeconds = java.time.Duration.between(start, end).seconds

            durationSeconds.toDouble() // Return duration in seconds
        } catch (e: Exception) {
            e.printStackTrace()
            0.0 // Fallback in case of errors
        }
    }


    // Funzione per aggiornare i dati (se necessario)
    fun updatePredictedActivityData(predictions: List<ActivityPredictionWithDuration>) {
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

    data class ActivityPredictionWithDuration(
        val id: Int,
        val processedAt: String,
        val label: String,
        val durationMinutes: Double
    )
}