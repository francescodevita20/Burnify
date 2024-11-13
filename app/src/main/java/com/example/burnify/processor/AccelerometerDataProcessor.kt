package com.example.burnify.processor

import com.example.burnify.model.AccelerometerMeasurements
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sqrt
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class AccelerometerDataProcessor {

    fun processMeasurements(measurements: AccelerometerMeasurements): Map<String, Any> {
        val samples = measurements.getSamples()
        val xValues = samples.map { it.getSampleValues().first }
        val yValues = samples.map { it.getSampleValues().second }
        val zValues = samples.map { it.getSampleValues().third }
        val magnitudes = samples.map { sqrt(it.getSampleValues().first.pow(2) + it.getSampleValues().second.pow(2) + it.getSampleValues().third.pow(2)) }

        val currentDateTime = LocalDateTime.now()
        val formattedDateTime = currentDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

        // Aggiungi la data e l'orario ai risultati
        return mapOf(
            "ProcessedAt" to formattedDateTime,
            "MeanX" to calculateMean(xValues),
            "MeanY" to calculateMean(yValues),
            "MeanZ" to calculateMean(zValues),
            "StandardDeviationX" to calculateStandardDeviation(xValues),
            "StandardDeviationY" to calculateStandardDeviation(yValues),
            "StandardDeviationZ" to calculateStandardDeviation(zValues),
            "PercentilesX" to calculatePercentiles(xValues),
            "PercentilesY" to calculatePercentiles(yValues),
            "PercentilesZ" to calculatePercentiles(zValues),
            "ThirdMoment" to calculateMoment(magnitudes, 3),
            "FourthMoment" to calculateMoment(magnitudes, 4),
            "EntropyValue" to calculateEntropy(magnitudes, 8),
            "SpectralCharacteristics" to calculateSpectralCharacteristics(magnitudes),
            "Autocorrelation" to calculateAutocorrelation(magnitudes),
            "AxisCorrelation" to calculateAxisCorrelation(xValues, yValues, zValues)
        )
    }

    private fun calculateMean(values: List<Float>): Float {
        return values.sum() / values.size
    }

    private fun calculateStandardDeviation(values: List<Float>): Float {
        val mean = calculateMean(values)
        return sqrt(values.sumOf { (it - mean).pow(2).toDouble() }.toFloat() / values.size)
    }

    private fun calculateMoment(values: List<Float>, order: Int): Float {
        val mean = calculateMean(values)
        return values.sumOf { (it - mean).pow(order).toDouble() }.toFloat() / values.size
    }

    private fun calculatePercentiles(values: List<Float>): Map<String, Float> {
        val sorted = values.sorted()
        val n = values.size
        return mapOf(
            "25thPercentile" to sorted[(n * 0.25).toInt()],
            "50thPercentile" to sorted[(n * 0.5).toInt()],
            "75thPercentile" to sorted[(n * 0.75).toInt()]
        )
    }

    private fun calculateEntropy(values: List<Float>, bins: Int): Float {
        val min = values.minOrNull() ?: return 0f
        val max = values.maxOrNull() ?: return 0f
        val binWidth = (max - min) / bins

        // Count occurrences in each bin
        val binCounts = IntArray(bins)
        for (value in values) {
            val binIndex = ((value - min) / binWidth).toInt().coerceIn(0, bins - 1)
            binCounts[binIndex]++
        }

        // Calculate probabilities
        val total = values.size.toFloat()
        val probabilities = binCounts.map { it / total }

        // Calculate entropy
        return -probabilities.filter { it > 0 }
            .sumOf { p ->
                if (p > 0) {
                    p * ln(p.toDouble())  // Convert the log calculation to Double
                } else {
                    0.0  // Keep the value as Double
                }
            }.toFloat()  // Convert the final result to Float
    }

    private fun calculateSpectralCharacteristics(values: List<Float>): Map<String, Float> {
        val logEnergy = values.sumOf { it.pow(2).toDouble() }.let { ln(it) }.toFloat()
        val spectralEntropy = calculateEntropy(values,8)
        return mapOf("LogEnergy" to logEnergy, "SpectralEntropy" to spectralEntropy)
    }

    private fun calculateAutocorrelation(values: List<Float>, lag: Int = 1): Float {
        val n = values.size
        val mean = calculateMean(values)
        return (0 until n - lag).sumOf { ((values[it] - mean) * (values[it + lag] - mean)).toDouble() }.toFloat() / (n - lag)
    }

    private fun calculateAxisCorrelation(xValues: List<Float>, yValues: List<Float>, zValues: List<Float>): Map<String, Float> {
        val correlationXY = calculateCorrelation(xValues, yValues)
        val correlationXZ = calculateCorrelation(xValues, zValues)
        val correlationYZ = calculateCorrelation(yValues, zValues)
        return mapOf("CorrelationXY" to correlationXY, "CorrelationXZ" to correlationXZ, "CorrelationYZ" to correlationYZ)
    }

    private fun calculateCorrelation(values1: List<Float>, values2: List<Float>): Float {
        val mean1 = calculateMean(values1)
        val mean2 = calculateMean(values2)
        val stdDev1 = calculateStandardDeviation(values1)
        val stdDev2 = calculateStandardDeviation(values2)
        return values1.indices.sumOf { ((values1[it] - mean1) * (values2[it] - mean2)).toDouble() }
            .toFloat() / (stdDev1 * stdDev2 * values1.size)
    }

    // Funzione che ritorna tutti i valori calcolati come stringa
    fun getResultsAsString(measurements: AccelerometerMeasurements): String {


        val results = processMeasurements(measurements)

        return buildString {
            results.forEach { (key, value) ->
                appendLine("$key: $value")
            }
        }
    }
}
