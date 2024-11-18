package com.example.burnify.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accelerometer_processed_sample")
data class AccelerometerProcessedSample(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val processedAt: String,
    val meanX: Float,
    val meanY: Float,
    val meanZ: Float,
    val standardDeviationX: Float,
    val standardDeviationY: Float,
    val standardDeviationZ: Float,
    val percentile25X: Float,
    val percentile50X: Float,
    val percentile75X: Float,
    val percentile25Y: Float,
    val percentile50Y: Float,
    val percentile75Y: Float,
    val percentile25Z: Float,
    val percentile50Z: Float,
    val percentile75Z: Float,
    val thirdMoment: Float,
    val fourthMoment: Float,
    val entropyValue: Float,
    val spectralLogEnergy: Float,
    val spectralEntropy: Float,
    val autocorrelation: Float,
    val correlationXY: Float? = null, // Permetti NaN come valore valido
    val correlationXZ: Float? = null,
    val correlationYZ: Float? = null
) {
    // Funzione per stampare tutti i dati del singolo sample in una riga
    fun printSampleDetails(): String {
        return "ID: $id, Processed At: $processedAt, Mean X: $meanX, Mean Y: $meanY, Mean Z: $meanZ, " +
                "Standard Deviation X: $standardDeviationX, Y: $standardDeviationY, Z: $standardDeviationZ, " +
                "Percentile 25 X: $percentile25X, Y: $percentile25Y, Z: $percentile25Z, " +
                "Percentile 50 X: $percentile50X, Y: $percentile50Y, Z: $percentile50Z, " +
                "Percentile 75 X: $percentile75X, Y: $percentile75Y, Z: $percentile75Z, " +
                "Third Moment: $thirdMoment, Fourth Moment: $fourthMoment, " +
                "Entropy Value: $entropyValue, Spectral Log Energy: $spectralLogEnergy, " +
                "Spectral Entropy: $spectralEntropy, Autocorrelation: $autocorrelation, " +
                "Correlation XY: $correlationXY, XZ: $correlationXZ, YZ: $correlationYZ"
    }
}


@Entity(tableName = "gyroscope_processed_sample")
data class GyroscopeProcessedSample(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val processedAt: String,
    val meanX: Float,
    val meanY: Float,
    val meanZ: Float,
){
    fun printSampleDetails(): String {
    return "ID: $id, Processed At: $processedAt, Mean X: $meanX, Mean Y: $meanY, Mean Z: $meanZ, "
}}
