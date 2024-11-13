package com.example.burnify.processor

import com.example.burnify.model.GyroscopeMeasurements
import kotlin.math.sqrt
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class GyroscopeDataProcessor {

    fun processMeasurements(measurements: GyroscopeMeasurements): Map<String, Any> {
        val samples = measurements.getSamples()
        val xValues = samples.map { it.getSampleValues().first }
        val yValues = samples.map { it.getSampleValues().second }
        val zValues = samples.map { it.getSampleValues().third }

        val currentDateTime = LocalDateTime.now()
        val formattedDateTime = currentDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

        // Aggiungi la data e l'orario ai risultati
        return mapOf(
            "ProcessedAt" to formattedDateTime,
            "MeanX" to calculateMean(xValues),
            "MeanY" to calculateMean(yValues),
            "MeanZ" to calculateMean(zValues)
        )
    }

    private fun calculateMean(values: List<Float>): Float {
        return values.sum() / values.size
    }

    // Funzione che ritorna tutti i valori calcolati come stringa
    fun getResultsAsString(measurements: GyroscopeMeasurements): String {
        val results = processMeasurements(measurements)

        return buildString {
            results.forEach { (key, value) ->
                appendLine("$key: $value")
            }
        }
    }
}
