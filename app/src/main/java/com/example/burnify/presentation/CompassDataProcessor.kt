package com.example.burnify.presentation

import kotlin.math.sqrt
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class CompassDataProcessor {

    fun processMeasurements(measurements: CompassMeasurements): Map<String, Any> {
        val samples = measurements.getSamples()

        val angles = samples.map { it.getAngle() }

        // Ottieni la data e l'orario correnti
        val currentDateTime = LocalDateTime.now()
        val formattedDateTime = currentDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

        // Aggiungi la data e l'orario ai risultati
        return mapOf(
            "ProcessedAt" to formattedDateTime,
            "MeanAngle" to calculateMean(angles)
        )
    }

    private fun calculateMean(values: List<Float>): Float {
        return values.sum() / values.size
    }

    // Funzione che ritorna tutti i valori calcolati come stringa
    fun getResultsAsString(measurements: CompassMeasurements): String {
        val results = processMeasurements(measurements)
        return buildString {
            results.forEach { (key, value) ->
                appendLine("$key: $value")
            }
        }
    }
}
