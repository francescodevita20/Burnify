package com.example.burnify.viewmodel

import com.example.burnify.util.preprocessTimestamp
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.burnify.database.AppDatabaseProvider
import com.example.burnify.database.dao.ActivityPredictionDao
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class PredictedActivityViewModel(application: Application) : AndroidViewModel(application) {

    // StateFlow per una lista di ActivityPrediction con durata inclusa
    private val _predictedActivityData = MutableStateFlow<List<ActivityPredictionWithDuration>>(emptyList())
    val predictedActivityData: StateFlow<List<ActivityPredictionWithDuration>> get() = _predictedActivityData

    private val activityPredictionDao: ActivityPredictionDao =
        AppDatabaseProvider.getInstance(application).activityPredictionDao()

    fun loadPredictedActivityData() {
        viewModelScope.launch {
            try {
                val predictions = activityPredictionDao.getSamplesFromToday()

                // Calculate the duration for each prediction regardless of the label
                val predictionsWithDurations = predictions.mapIndexed { index, current ->
                    val next = predictions.getOrNull(index + 1)

                    // Preprocess the timestamps before calculating duration
                    val processedAt = preprocessTimestamp(current.processedAt)
                    val nextProcessedAt = next?.let { preprocessTimestamp(it.processedAt) }

                    // Calculate duration between current and next prediction
                    var durationMinutes = 0.0
                    if (nextProcessedAt != null) {
                        durationMinutes = calculateDuration(processedAt, nextProcessedAt)

                        // Check for a gap longer than a threshold (e.g., 1 hour or 60 minutes)
                        val gapDuration = calculateDuration(processedAt, nextProcessedAt)
                        if (gapDuration > 0.25) { // Adjust this threshold based on your needs
                            durationMinutes = 0.20 // Set duration to 0.10 if there is a long gap
                        }
                    }

                    // Append duration to label
                    ActivityPredictionWithDuration(
                        id = current.id,
                        processedAt = processedAt,
                        label = current.label ?: "", // Provide default empty string if label is null
                        durationMinutes = durationMinutes
                    )
                }

                // Emit updated predictions with durations to StateFlow
                _predictedActivityData.value = predictionsWithDurations
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
            val durationSeconds = java.time.Duration.between(end, start).seconds
            durationSeconds / 60.0 // Convert seconds to fractional minutes
        } catch (e: Exception) {
            e.printStackTrace()
            0.0 // Fallback in case of errors
        }
    }

    data class ActivityPredictionWithDuration(
        val id: Int,
        val processedAt: String,
        val label: String,
        val durationMinutes: Double
    )
}
