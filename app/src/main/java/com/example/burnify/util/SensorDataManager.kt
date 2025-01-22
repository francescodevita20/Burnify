package com.example.burnify.util

import ai.onnxruntime.OnnxJavaType
import android.content.Context
import com.example.burnify.viewmodel.LastPredictionViewModel
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import ai.onnxruntime.TensorInfo
import com.example.burnify.database.ActivityPrediction
import com.example.burnify.database.AppDatabaseProvider
import com.example.burnify.database.dao.ActivityPredictionDao
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.reflect.typeOf

object SensorDataManager {
    var lastPredictionViewModel: LastPredictionViewModel? = null

    private val accelerometerData: MutableList<List<Float>> = mutableListOf()
    private val gyroscopeData: MutableList<List<Float>> = mutableListOf()
    private val magnetometerData: MutableList<List<Float>> = mutableListOf()

    private const val requiredDataPoints = 50

    fun updateAccelerometerData(accelerometerMeasurement: List<Float>, context: Context) {
        if (accelerometerData.size < requiredDataPoints) {
            accelerometerData.add(accelerometerMeasurement)
        }
        checkAndProcessData(context)
    }

    fun updateGyroscopeData(gyroscopeMeasurement: List<Float>, context: Context) {
        if (gyroscopeData.size < requiredDataPoints) {
            gyroscopeData.add(gyroscopeMeasurement)
        }
        checkAndProcessData(context)
    }

    fun updateMagnetometerData(magnetometerMeasurement: List<Float>, context: Context) {
        if (magnetometerData.size < requiredDataPoints) {
            magnetometerData.add(magnetometerMeasurement)
        }
        checkAndProcessData(context)
    }

    private fun checkAndProcessData(context: Context) {
        if (accelerometerData.size >= requiredDataPoints &&
            gyroscopeData.size >= requiredDataPoints &&
            magnetometerData.size >= requiredDataPoints
        ) {
            val unifiedSensorData = combineSensorData(accelerometerData, gyroscopeData, magnetometerData)
            val extractedFeatures = extractFeatures(unifiedSensorData)

            // Stampa del numero di caratteristiche estratte
            println("Number of extracted features: ${extractedFeatures.size}")

            if (extractedFeatures.size == 36) {
                val prediction = runModelOnDevice(extractedFeatures, context)
                lastPredictionViewModel?.updateLastPredictionData(prediction)
            } else {
                lastPredictionViewModel?.updateLastPredictionData("Error: Incorrect feature size")
            }

            resetSensorData()
        }
    }

    private fun combineSensorData(
        accelerometerData: List<List<Float>>,
        gyroscopeData: List<List<Float>>,
        magnetometerData: List<List<Float>>
    ): List<List<Float>> {
        val unifiedData: MutableList<List<Float>> = mutableListOf()
        for (i in 0 until requiredDataPoints) {
            val row: MutableList<Float> = mutableListOf()
            row.addAll(accelerometerData[i])
            row.addAll(gyroscopeData[i])
            row.addAll(magnetometerData[i])
            unifiedData.add(row)
        }
        return unifiedData
    }

    private fun extractFeatures(sensorData: List<List<Float>>): List<Float> {
        val featureList = mutableListOf<Float>()

        for (i in 0 until 9) {  // 9 features dai sensori (acc_X, acc_Y, acc_Z, mag_X, mag_Y, mag_Z, gyro_X, gyro_Y, gyro_Z)
            val featureValues = sensorData.map { it[i] }
            featureList.add(featureValues.average().toFloat()) // Mean
            featureList.add(featureValues.standardDeviation().toFloat()) // Std
            featureList.add(featureValues.minOrNull() ?: 0f) // Min
            featureList.add(featureValues.maxOrNull() ?: 0f) // Max
        }

        return featureList
    }

    private fun runModelOnDevice(features: List<Float>, context: Context): String {
        var predictedValue: Long? = 0
        val env = OrtEnvironment.getEnvironment()
        return try {
            val modelBytes = context.assets.open("model.onnx").readBytes()
            val session = env.createSession(modelBytes)

            // Check and print feature size
            if (features.size != 36) {
                throw IllegalArgumentException("Expected 36 features, but got ${features.size}")
            }


            // Prepare input as [1, 36]
            val inputData = arrayOf(features.toFloatArray())


            val tensor = OnnxTensor.createTensor(env, inputData)


            val inputs = mapOf("float_input" to tensor)


            val result = session.run(inputs)

            // Retrieve the output
            val outputName = session.outputNames.first()


            // Get the result, handling the Optional correctly
            val outputOptional = result[outputName]
            // Verifica se il valore è presente e stampalo


            if (outputOptional.isPresent) {
                val outputValue = outputOptional.get()
                if (outputValue is OnnxTensor) {
                    // Verifica che il tensore contenga dati di tipo INT64
                    if (outputValue.info.type == OnnxJavaType.INT64) {
                        val outputIntValue = outputValue.getValue() as LongArray  // Cast esplicito a LongArray
                        predictedValue = outputIntValue[0]  // Estrai il primo elemento


                    } else {
                        println("Error: Unexpected output type. Expected INT64 but got ${outputValue.info.onnxType}")
                    }
                    outputValue.close()
                } else {
                    println("Error: Output is not an OnnxTensor.")
                }
                // Check if the floatBuffer is null
                if (predictedValue == null) {
                    throw IllegalStateException("Output tensor floatBuffer is null.")
                }
                val predictedActivity = convertLabelToActivity(predictedValue)
                lastPredictionViewModel?.updateLastPredictionData(predictedActivity)

                // Insert prediction into the database
                GlobalScope.launch {
                    val db = AppDatabaseProvider.getInstance(context)
                    val dao = db.activityPredictionDao()
                    insertActivityPredictionToDB(dao, predictedActivity)
                }
                return predictedActivity
            } else {
                throw IllegalStateException("Output is not an OnnxTensor.")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return "Error during inference: ${e.message}"
        }
    }


    private fun resetSensorData() {
        accelerometerData.clear()
        gyroscopeData.clear()
        magnetometerData.clear()
    }

    private fun List<Float>.standardDeviation(): Double {
        val mean = this.average()
        return sqrt(this.map { (it - mean).pow(2) }.average())
    }
    private fun  convertLabelToActivity(prediction: Long): String {
        return when (prediction) {
            0L -> "downstairs"
            1L -> "running"
            2L -> "standing"
            3L -> "upstairs"
            4L -> "walking"
            else -> "unknown"
    }}

    suspend fun insertActivityPredictionToDB(dao: ActivityPredictionDao, label: String) {
        val currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

        val activityPrediction = ActivityPrediction(
            processedAt = currentDateTime, // The current timestamp
            label = label // The predicted activity label
        )

        dao.insertActivityPrediction(activityPrediction)
    }
}


