package com.example.burnify.processor

import com.example.burnify.model.MagnetometerMeasurements
import kotlin.math.sqrt
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MagnetometerDataProcessor {

    fun processMeasurements(measurements: MagnetometerMeasurements): Map<String, Any> {
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
            "MeanZ" to calculateMean(zValues),
            "Magnitude" to calculateMagnitude(xValues, yValues, zValues)
        )
    }

    private fun calculateMean(values: List<Float>): Float {
        return if (values.isNotEmpty()) values.sum() / values.size else 0f
    }

    // Funzione per calcolare la magnitudine media
    private fun calculateMagnitude(xValues: List<Float>, yValues: List<Float>, zValues: List<Float>): Float {
        val magnitudes = xValues.zip(yValues.zip(zValues)).map { (x, yz) ->
            val (y, z) = yz
            sqrt(x * x + y * y + z * z)
        }
        return calculateMean(magnitudes)
    }

    // Funzione che ritorna tutti i valori calcolati come stringa
    fun getResultsAsString(measurements: MagnetometerMeasurements): String {
        val results = processMeasurements(measurements)

        return buildString {
            results.forEach { (key, value) ->
                appendLine("$key: $value")
            }
        }
    }
}
