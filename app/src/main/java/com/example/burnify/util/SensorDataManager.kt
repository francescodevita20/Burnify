package com.example.burnify.util

import android.app.Application
import android.content.Context
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.burnify.database.dao.ActivityPredictionDao
import com.example.burnify.database.ActivityPrediction
import com.example.burnify.database.AppDatabaseProvider
import com.example.burnify.model.AccelerometerMeasurements
import com.example.burnify.model.GyroscopeMeasurements
import com.example.burnify.model.MagnetometerMeasurements
import com.example.burnify.viewmodel.AccelerometerViewModel
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
    // Flags to check if sensor data has been filled
    var accelerometerIsFilled: Boolean = false
    var gyroscopeIsFilled: Boolean = false
    var magnetometerIsFilled: Boolean = false
    var lastPredictionViewModel: LastPredictionViewModel? = null



    // Tensor to hold the sensor data (500 rows, 9 columns)
    var outputTensor: MutableList<MutableList<Float>> = createZeroMatrix().toMutableList()

    /**
     * Sets accelerometer measurements to the output tensor.
     * @param accelerometerMeasurements The accelerometer measurements.
     * @param context The application context.
     */
    fun setAccelerometerMeasurements(accelerometerMeasurements: AccelerometerMeasurements, context: Context) {
        val samples = accelerometerMeasurements.getSamples()

        // If there are enough samples (500), fill the tensor with data
        if (samples.size >= 500) {
            for (i in 0 until 500) {
                outputTensor[i][0] = samples[i][0] // X-axis
                outputTensor[i][1] = samples[i][1] // Y-axis
                outputTensor[i][2] = samples[i][2] // Z-axis
            }
            println("Accelerometer service is stopped")

            // If all sensor data is filled, send the data to the server
            if (outputTensorIsFull()) {
                makePostRequestWithSensorData(context)
                resetStateFlags()
                outputTensor = createZeroMatrix()
                println("Service states are resumed")
            }
        } else {
            println("Insufficient accelerometer data to fill the output tensor")
        }
    }

    /**
     * Sets gyroscope measurements to the output tensor.
     * @param gyroscopeMeasurements The gyroscope measurements.
     * @param context The application context.
     */
    fun setGyroscopeMeasurements(gyroscopeMeasurements: GyroscopeMeasurements, context: Context) {
        val samples = gyroscopeMeasurements.getSamples()

        // If there are enough samples (500), fill the tensor with data
        if (samples.size >= 500) {
            for (i in 0 until 500) {
                outputTensor[i][3] = samples[i][0] // X-axis
                outputTensor[i][4] = samples[i][1] // Y-axis
                outputTensor[i][5] = samples[i][2] // Z-axis
            }
            gyroscopeIsFilled = true
            println("Gyroscope service is stopped")

            // If all sensor data is filled, send the data to the server
            if (outputTensorIsFull()) {
                makePostRequestWithSensorData(context)
                resetStateFlags()
                outputTensor = createZeroMatrix()
                println("Service states are resumed")
            }
        } else {
            println("Insufficient gyroscope data to fill the output tensor")
        }
    }

    /**
     * Sets magnetometer measurements to the output tensor.
     * @param magnetometerMeasurements The magnetometer measurements.
     * @param context The application context.
     */
    fun setMagnetometerMeasurements(magnetometerMeasurements: MagnetometerMeasurements, context: Context) {
        val samples = magnetometerMeasurements.getSamples()

        // If there are enough samples (500), fill the tensor with data
        if (samples.size >= 500) {
            for (i in 0 until 500) {
                outputTensor[i][6] = samples[i][0] // X-axis
                outputTensor[i][7] = samples[i][1] // Y-axis
                outputTensor[i][8] = samples[i][2] // Z-axis
            }
            magnetometerIsFilled = true
            println("Magnetometer service is stopped")

            // If all sensor data is filled, send the data to the server
            if (outputTensorIsFull()) {
                makePostRequestWithSensorData(context)
                resetStateFlags()
                outputTensor = createZeroMatrix()
                println("Service states are resumed")
            }
        } else {
            println("Insufficient magnetometer data to fill the output tensor")
        }
    }

    /**
     * Checks if the output tensor is fully filled with data from all sensors.
     * @return True if all sensor data is filled, false otherwise.
     */
    private fun outputTensorIsFull(): Boolean {
        return accelerometerIsFilled && gyroscopeIsFilled && magnetometerIsFilled
    }

    /**
     * Creates a zero-initialized matrix of size 500x9.
     * @return A 500x9 matrix filled with zeros.
     */
    private fun createZeroMatrix(): MutableList<MutableList<Float>> {
        val rows = 500
        val columns = 9
        return MutableList(rows) {
            MutableList(columns) { 0.0f }
        }
    }

    /**
     * Sends the sensor data (output tensor) to the server via a POST request.
     * @param context The application context.
     */
    private fun makePostRequestWithSensorData(context: Context) {
        val jsonData = convertOutputTensorToJson()
        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val body = RequestBody.create(mediaType, jsonData)

        val request = Request.Builder()
            .url("http://10.0.2.2:8000/predict/")  // Replace with your server's URL
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
                            addPredictionToSharedPreferences(context, predictedClass, "predictions")


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
     * Converts the output tensor into a JSON string.
     * @return The output tensor as a JSON string.
     */
    private fun convertOutputTensorToJson(): String {
        val jsonArray = JSONArray()

        for (i in 0 until 500) {
            val row = JSONArray()
            for (j in 0 until 9) {
                row.put(outputTensor[i][j])
            }
            jsonArray.put(row)
        }

        val jsonObject = JSONObject()
        jsonObject.put("data", jsonArray)
        println(jsonObject.toString())
        return jsonObject.toString()
    }

    /**
     * Resets the flags that track whether sensor data has been filled.
     */
    private fun resetStateFlags() {
        accelerometerIsFilled = false
        gyroscopeIsFilled = false
        magnetometerIsFilled = false
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
}
