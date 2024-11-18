package com.example.burnify.processor

import com.example.burnify.model.GyroscopeMeasurements
import com.example.burnify.model.GyroscopeProcessedSample
import com.example.burnify.model.MagnetometerMeasurements
import com.example.burnify.model.MagnetometerProcessedSample
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sqrt
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MagnetometerDataProcessor {

    fun processMeasurementsToEntity(measurements: MagnetometerMeasurements): MagnetometerProcessedSample {
        val samples = measurements.getSamples()
        val xValues = samples.map { it.getSampleValues().first }
        val yValues = samples.map { it.getSampleValues().second }
        val zValues = samples.map { it.getSampleValues().third }
        val magnitudes = samples.map { sqrt(it.getSampleValues().first.pow(2) + it.getSampleValues().second.pow(2) + it.getSampleValues().third.pow(2)) }

        val currentDateTime = LocalDateTime.now()
        val formattedDateTime = currentDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

        return MagnetometerProcessedSample(
            processedAt = formattedDateTime,
            meanX = calculateMean(xValues),
            meanY = calculateMean(yValues),
            meanZ = calculateMean(zValues)
        )
    }

    private fun calculateMean(values: List<Float>): Float {
        return values.sum() / values.size
    }


    // Funzione che ritorna tutti i valori calcolati come stringa
    fun getResultsAsString(measurements: MagnetometerMeasurements): String {
        // Elabora i dati in un oggetto GyroscopeProcessedSample
        val processedSample = processMeasurementsToEntity(measurements)

        return buildString {
            appendLine("ProcessedAt: ${processedSample.processedAt}")
            appendLine("MeanX: ${processedSample.meanX}")
            appendLine("MeanY: ${processedSample.meanY}")
            appendLine("MeanZ: ${processedSample.meanZ}")
        }
    }

}


