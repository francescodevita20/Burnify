package com.example.burnify.processor

import com.example.burnify.model.GyroscopeMeasurements
import com.example.burnify.database.GyroscopeProcessedSample
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sqrt
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import org.apache.commons.math3.transform.FastFourierTransformer
import org.apache.commons.math3.transform.DftNormalization
import org.apache.commons.math3.transform.TransformType

class GyroscopeDataProcessor {

    fun processMeasurementsToEntity(measurements: GyroscopeMeasurements): GyroscopeProcessedSample {
        val samples = measurements.getSamples()
        val xValues = samples.map { it.get(0)  }
        val yValues = samples.map { it.get(1)  }
        val zValues = samples.map { it.get(2)  }
        val magnitudes = samples.map { sqrt(it.get(0).pow(2) + it.get(1).pow(2) + it.get(2).pow(2)) }

        val currentDateTime = LocalDateTime.now()
        val formattedDateTime = currentDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        val spectralLogEnergyBands = calculateSpectralLogEnergyBands(magnitudes)
        return GyroscopeProcessedSample(
            processedAt = formattedDateTime,
            meanX = calculateMean(xValues),
            meanY = calculateMean(yValues),
            meanZ = calculateMean(zValues),
            standardDeviationX = calculateStandardDeviation(xValues),
            standardDeviationY = calculateStandardDeviation(yValues),
            standardDeviationZ = calculateStandardDeviation(zValues),
            percentile25X = calculatePercentiles(xValues)["25thPercentile"] ?: 0f,
            percentile50X = calculatePercentiles(xValues)["50thPercentile"] ?: 0f,
            percentile75X = calculatePercentiles(xValues)["75thPercentile"] ?: 0f,
            percentile25Y = calculatePercentiles(yValues)["25thPercentile"] ?: 0f,
            percentile50Y = calculatePercentiles(yValues)["50thPercentile"] ?: 0f,
            percentile75Y = calculatePercentiles(yValues)["75thPercentile"] ?: 0f,
            percentile25Z = calculatePercentiles(zValues)["25thPercentile"] ?: 0f,
            percentile50Z = calculatePercentiles(zValues)["50thPercentile"] ?: 0f,
            percentile75Z = calculatePercentiles(zValues)["75thPercentile"] ?: 0f,
            thirdMoment = calculateMoment(magnitudes, 3),
            fourthMoment = calculateMoment(magnitudes, 4),
            entropyValue = calculateEntropy(magnitudes, 8),
            spectralEntropy = calculateSpectralCharacteristics(magnitudes)["SpectralEntropy"] ?: 0f,
            autocorrelation = calculateAutocorrelation(magnitudes),
            correlationXY = calculateCorrelation(xValues, yValues),
            correlationXZ = calculateCorrelation(xValues, zValues),
            correlationYZ = calculateCorrelation(yValues, zValues),
            energyBand0_0_5Hz = spectralLogEnergyBands["0-0.5Hz"] ?: 0f,
            energyBand0_5_1Hz = spectralLogEnergyBands["0.5-1Hz"] ?: 0f,
            energyBand1_3Hz = spectralLogEnergyBands["1-3Hz"] ?: 0f,
            energyBand3_5Hz = spectralLogEnergyBands["3-5Hz"] ?: 0f,
            energyBand5HzPlus = spectralLogEnergyBands[">5Hz"] ?: 0f
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

        val binCounts = IntArray(bins)
        for (value in values) {
            val binIndex = ((value - min) / binWidth).toInt().coerceIn(0, bins - 1)
            binCounts[binIndex]++
        }

        val total = values.size.toFloat()
        val probabilities = binCounts.map { it / total }

        return -probabilities.filter { it > 0 }
            .sumOf { p -> p * ln(p.toDouble()) }.toFloat()
    }

    private fun calculateSpectralCharacteristics(values: List<Float>): Map<String, Float> {
        val logEnergy = values.sumOf { it.pow(2).toDouble() }.let { ln(it) }.toFloat()
        val spectralEntropy = calculateEntropy(values, 8)
        return mapOf("LogEnergy" to logEnergy, "SpectralEntropy" to spectralEntropy)
    }

    private fun calculateAutocorrelation(values: List<Float>, lag: Int = 1): Float {
        val n = values.size
        val mean = calculateMean(values)
        return (0 until n - lag).sumOf { ((values[it] - mean) * (values[it + lag] - mean)).toDouble() }.toFloat() / (n - lag)
    }

    private fun calculateCorrelation(values1: List<Float>, values2: List<Float>): Float {
        val mean1 = calculateMean(values1)
        val mean2 = calculateMean(values2)
        val stdDev1 = calculateStandardDeviation(values1)
        val stdDev2 = calculateStandardDeviation(values2)
        return values1.indices.sumOf { ((values1[it] - mean1) * (values2[it] - mean2)).toDouble() }
            .toFloat() / (stdDev1 * stdDev2 * values1.size)
    }

    private fun calculateSpectralLogEnergyBands(values: List<Float>): Map<String, Float> {
        val doubleValues = values.map { it.toDouble() }.toDoubleArray()
        val fft = FastFourierTransformer(DftNormalization.STANDARD)

        val fftResult = fft.transform(doubleValues, TransformType.FORWARD)

        val bandEnergies = DoubleArray(5) { 0.0 }
        val samplingRate = 50.0
        val frequencies = (0 until fftResult.size / 2).map { it * (samplingRate / fftResult.size) }

        for (i in frequencies.indices) {
            val freq = frequencies[i]
            val magnitude = fftResult[i].abs()
            when {
                freq <= 0.5 -> bandEnergies[0] += magnitude.pow(2)
                freq <= 1.0 -> bandEnergies[1] += magnitude.pow(2)
                freq <= 3.0 -> bandEnergies[2] += magnitude.pow(2)
                freq <= 5.0 -> bandEnergies[3] += magnitude.pow(2)
                else -> bandEnergies[4] += magnitude.pow(2)
            }
        }

        return mapOf(
            "0-0.5Hz" to bandEnergies[0].toFloat(),
            "0.5-1Hz" to bandEnergies[1].toFloat(),
            "1-3Hz" to bandEnergies[2].toFloat(),
            "3-5Hz" to bandEnergies[3].toFloat(),
            ">5Hz" to bandEnergies[4].toFloat()
        )
    }
}
