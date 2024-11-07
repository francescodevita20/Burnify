package com.example.burnify.presentation

import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sqrt
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class AccelerometerDataProcessor {

    fun processMeasurements(measurements: AccelerometerMeasurements): Map<String, Any> {
        val samples = measurements.getSamples()

        val xValues = samples.map { it.getX() }
        val yValues = samples.map { it.getY() }
        val zValues = samples.map { it.getZ() }
        val magnitudes = samples.map { sqrt(it.getX().pow(2) + it.getY().pow(2) + it.getZ().pow(2)) }

        // Ottieni la data e l'orario correnti
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
            "EntropyValue" to calculateEntropy(magnitudes),
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

    private fun calculateEntropy(values: List<Float>): Float {
        val frequencyMap = values.groupingBy { it }.eachCount().mapValues { it.value / values.size.toFloat() }
        return -frequencyMap.values.map { p ->
            if (p > 0) {
                p * ln(p.toDouble()) // Converte solo il calcolo del logaritmo in Double
            } else {
                0.0 // Mantieni il valore come Double
            }
        }.sum().toFloat() // Somma i valori e converte il risultato finale in Float
    }

    private fun calculateSpectralCharacteristics(values: List<Float>): Map<String, Float> {
        val logEnergy = values.sumOf { it.pow(2).toDouble() }.let { ln(it) }.toFloat()
        val spectralEntropy = calculateEntropy(values)
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
