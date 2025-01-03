package com.example.burnify.util

import android.content.Context
import com.example.burnify.database.dao.ActivityPredictionDao
import com.example.burnify.database.ActivityPrediction
import com.example.burnify.database.AppDatabaseProvider
import com.example.burnify.viewmodel.LastPredictionViewModel
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object SensorDataManager {
    var lastPredictionViewModel: LastPredictionViewModel? = null

    // Store the accelerometer data and other sensor data until all are collected
    private val accelerometerData: MutableList<List<Float>> = mutableListOf() // List to hold accelerometer data
    private val gyroscopeData: MutableList<List<Float>> = mutableListOf() // List for gyroscope data
    private val magnetometerData: MutableList<List<Float>> = mutableListOf() // List for magnetometer data

    private const val requiredDataPoints = 200 // Number of data points to collect

    /**
     * Updates the accelerometer data (called when new accelerometer data arrives).
     * @param accelerometerMeasurement The accelerometer measurement (x, y, z).
     * @param context The application context.
     */
    fun updateAccelerometerData(accelerometerMeasurement: List<Float>, context: Context) {
        if (accelerometerData.size < requiredDataPoints) {
            accelerometerData.add(accelerometerMeasurement)
        }
        checkAndSendData(context)
    }

    /**
     * Updates the gyroscope data (called when new gyroscope data arrives).
     * @param gyroscopeMeasurement The gyroscope measurement (x, y, z).
     * @param context The application context.
     */
    fun updateGyroscopeData(gyroscopeMeasurement: List<Float>, context: Context) {
        if (gyroscopeData.size < requiredDataPoints) {
            gyroscopeData.add(gyroscopeMeasurement)
        }
        checkAndSendData(context)
    }

    /**
     * Updates the magnetometer data (called when new magnetometer data arrives).
     * @param magnetometerMeasurement The magnetometer measurement (x, y, z).
     * @param context The application context.
     */
    fun updateMagnetometerData(magnetometerMeasurement: List<Float>, context: Context) {
        if (magnetometerData.size < requiredDataPoints) {
            magnetometerData.add(magnetometerMeasurement)
        }
        checkAndSendData(context)
    }

    /**
     * Checks if all data is collected and ready to send.
     * @param context The application context.
     */
    private fun checkAndSendData(context: Context) {
        // If we have enough data from all sensors
        if (accelerometerData.size >= requiredDataPoints &&
            gyroscopeData.size >= requiredDataPoints &&
            magnetometerData.size >= requiredDataPoints
        ) {
            // Now all sensor data is collected. Proceed to send the data.
            val unifiedSensorData = combineSensorData(accelerometerData, gyroscopeData, magnetometerData)

            // Send the complete data to the server
            sendDataToServer(unifiedSensorData, context)

            // Reset all data after sending it
            resetSensorData()
        }
    }

    /**
     * Combines accelerometer, gyroscope, and magnetometer data into a unified sensor data format.
     * @param accelerometerData The accelerometer data.
     * @param gyroscopeData The gyroscope data.
     * @param magnetometerData The magnetometer data.
     * @return The unified sensor data (500 rows, 9 columns).
     */
    private fun combineSensorData(
        accelerometerData: List<List<Float>>,
        gyroscopeData: List<List<Float>>,
        magnetometerData: List<List<Float>>
    ): List<List<Float>> {
        val unifiedData: MutableList<List<Float>> = mutableListOf()

        for (i in 0 until requiredDataPoints) {
            val row: MutableList<Float> = mutableListOf()

            // Add accelerometer data (x, y, z)
            row.addAll(accelerometerData[i])

            // Add gyroscope data (x, y, z)
            row.addAll(gyroscopeData[i])

            // Add magnetometer data (x, y, z)
            row.addAll(magnetometerData[i])

            unifiedData.add(row)
        }

        return unifiedData
    }

    /**
     * Sends the complete sensor data to the server via a POST request.
     * @param unifiedSensorData The combined sensor data.
     * @param context The application context.
     */
    private fun sendDataToServer(unifiedSensorData: List<List<Float>>, context: Context) {
        val jsonData = convertDataToJson(unifiedSensorData)
        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val body = RequestBody.create(mediaType, jsonData)

        println("BODYYY: $jsonData")

        val request = Request.Builder()
            .url("https://3e64-2a05-d014-175c-b600-2d44-16ea-1c47-12ca.ngrok-free.app/predict/")  // Replace with your server's URL
            .post(body)
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("HttpRequests: POST request failed. Error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val bodyString = response.body?.string()
                    if (bodyString != null) {
                        println("HttpRequests: POST request successful. Response: $bodyString")
                        try {
                            val jsonObject = JSONObject(bodyString)
                            val predictedClass = jsonObject.getInt("predicted_class")
                            println("Predicted class: $predictedClass")

                            // Update the LastPredictionViewModel
                            lastPredictionViewModel?.updateLastPredictionData(predictedClass)

                            // Insert prediction into the database
                            GlobalScope.launch {
                                val db = AppDatabaseProvider.getInstance(context)
                                val dao = db.activityPredictionDao()
                                insertActivityPredictionToDB(dao, predictedClass)
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            println("Error parsing JSON response: ${e.message}")
                        }
                    } else {
                        println("Response body is null")
                    }
                } else {
                    println("HttpRequests: POST request failed. Error code: ${response.code}")
                }
            }
        })
    }

    /**
     * Converts the unified sensor data into a JSON string.
     * @param unifiedSensorData The unified sensor data (500 rows, 9 columns).
     * @return The unified sensor data as a JSON string.
     */
    private fun convertDataToJson(unifiedSensorData: List<List<Float>>): String {
        val jsonArray = JSONArray()

        for (row in unifiedSensorData) {
            val jsonRow = JSONArray()
            for (value in row) {
                jsonRow.put(value)
            }
            jsonArray.put(jsonRow)
        }

        val jsonObject = JSONObject()
        jsonObject.put("data", jsonArray)
        return jsonObject.toString()
    }

    /**
     * Inserts the predicted activity label into the database.
     * @param dao The activity prediction DAO.
     * @param label The predicted activity label.
     */
    suspend fun insertActivityPredictionToDB(dao: ActivityPredictionDao, label: Int) {
        val currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

        val activityPrediction = ActivityPrediction(
            processedAt = currentDateTime, // The current timestamp
            label = label // The predicted activity label
        )

        dao.insertActivityPrediction(activityPrediction)
    }

    /**
     * Resets all stored sensor data after sending it to the server.
     */
    private fun resetSensorData() {
        accelerometerData.clear()
        gyroscopeData.clear()
        magnetometerData.clear()
    }
}
