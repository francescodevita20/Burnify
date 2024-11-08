package com.example.burnify.presentation

import kotlin.math.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class CompassDataProcessor {

    fun processMeasurements(measurements: CompassMeasurements): Map<String, Any> {
        val samples = measurements.getSamples()
        val angles = samples.map { it.getAngle() }
        val sineValues = angles.map { sin(it) }
        val cosineValues = angles.map { cos(it) }

        val meanSine = calculateMean(sineValues)
        val stdDevSine = calculateStandardDeviation(sineValues, meanSine)
        val thirdMomentSine = calculateMoment(sineValues, meanSine, 3)
        val fourthMomentSine = calculateMoment(sineValues, meanSine, 4)

        val meanCosine = calculateMean(cosineValues)
        val stdDevCosine = calculateStandardDeviation(cosineValues, meanCosine)
        val thirdMomentCosine = calculateMoment(cosineValues, meanCosine, 3)
        val fourthMomentCosine = calculateMoment(cosineValues, meanCosine, 4)

        val entropy = calculateEntropy(angles, 8)

        // Current date and time
        val currentDateTime = LocalDateTime.now()
        val formattedDateTime = currentDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

        // Compile results
        return mapOf(
            "ProcessedAt" to formattedDateTime,
            "MeanSine" to meanSine,
            "StdDevSine" to stdDevSine,
            "ThirdMomentSine" to thirdMomentSine,
            "FourthMomentSine" to fourthMomentSine,
            "MeanCosine" to meanCosine,
            "StdDevCosine" to stdDevCosine,
            "ThirdMomentCosine" to thirdMomentCosine,
            "FourthMomentCosine" to fourthMomentCosine,
            "Entropy" to entropy
        )
    }

    private fun calculateMean(values: List<Float>): Float {
        return values.sum() / values.size
    }

    private fun calculateStandardDeviation(values: List<Float>, mean: Float): Float {
        val variance = values.map { (it - mean).pow(2) }.sum() / values.size
        return sqrt(variance)
    }

    private fun calculateMoment(values: List<Float>, mean: Float, order: Int): Float {
        return values.map { (it - mean).pow(order) }.sum() / values.size
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

    // Get all results as a formatted string
    fun getResultsAsString(measurements: CompassMeasurements): String {
        val results = processMeasurements(measurements)
        return buildString {
            results.forEach { (key, value) ->
                appendLine("$key: $value")
            }
        }
    }
}
