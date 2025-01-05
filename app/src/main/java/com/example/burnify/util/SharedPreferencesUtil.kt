package com.example.burnify.util

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.json.JSONArray

fun setSharedPreferences(context: Context, newMap: Map<String, Any>, sharedPreferencesName: String, dataKey: String) {
    try {
        val sharedPreferences = context.getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val gson = GsonBuilder().serializeSpecialFloatingPointValues().create()
        val updatedJson = gson.toJson(newMap)

        // Save the map under a specific key within the preferences file
        editor.putString(dataKey, updatedJson)
        editor.apply()

        // Log the data being saved for debugging
        println("Saving data to SharedPreferences ($sharedPreferencesName): $updatedJson")
    } catch (e: Exception) {
        println("Error saving map: ${e.message}")
    }
}

fun getSharedPreferences(context: Context, sharedPreferencesName: String, dataKey: String): Map<String, Any>? {
    return try {
        val sharedPreferences = context.getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE)
        val json = sharedPreferences.getString(dataKey, null) // Retrieve data using the specific key

        if (json.isNullOrEmpty()) {
            // No data found, returning null instead of default values
            println("No data found in SharedPreferences for key: $dataKey in $sharedPreferencesName.")
            null
        } else {
            val gson = Gson()
            val type = object : TypeToken<Map<String, Any>>() {}.type
            val map: Map<String, Any> = gson.fromJson(json, type)

            // Log the data retrieved for debugging
            println("Retrieved data from SharedPreferences ($sharedPreferencesName): $map")
            map
        }
    } catch (e: Exception) {
        println("Error retrieving data: ${e.message}. Returning null.")
        null
    }
}

fun clearSharedPreferences(context: Context, sharedPreferencesName: String) {
    try {
        val sharedPreferences = context.getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        editor.clear()
        editor.apply()

        println("SharedPreferences cleared successfully.")
    } catch (e: Exception) {
        println("Error clearing SharedPreferences: ${e.message}")
    }
}

/**
 * Adds a prediction to the SharedPreferences, keeping only the last 5 predictions.
 * @param context The application context.
 * @param predictedClass The predicted class to add.
 * @param sharedPreferencesName The name of the SharedPreferences.
 */
fun addPredictionToSharedPreferences(context: Context, predictedClass: Int, sharedPreferencesName: String) {
    try {
        val sharedPreferences = context.getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Retrieve existing predictions
        val predictionsJson = sharedPreferences.getString("last_predictions", "[]")
        val predictionsArray = JSONArray(predictionsJson)

        // Add the new prediction
        predictionsArray.put(predictedClass)

        // Keep only the last 5 predictions
        if (predictionsArray.length() > 5) {
            predictionsArray.remove(0)
        }

        // Save the updated predictions
        editor.putString("last_predictions", predictionsArray.toString())
        editor.apply()

        println("Prediction added successfully: $predictedClass. Updated predictions: $predictionsArray")
    } catch (e: Exception) {
        println("Error adding prediction: ${e.message}")
    }
}

/**
 * Retrieves the last 5 predictions from SharedPreferences.
 * @param context The application context.
 * @param sharedPreferencesName The name of the SharedPreferences.
 * @return A list of the last 5 predictions.
 */
fun getLastPredictionsFromSharedPreferences(context: Context, sharedPreferencesName: String): List<Int> {
    return try {
        val sharedPreferences = context.getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE)
        val predictionsJson = sharedPreferences.getString("last_predictions", "[]")
        val predictionsArray = JSONArray(predictionsJson)

        Log.d("DataScreen", "Retrieved predictions JSON: $predictionsJson")

        // Convert JSONArray to a list of integers
        val predictionsList = mutableListOf<Int>()
        for (i in 0 until predictionsArray.length()) {
            predictionsList.add(predictionsArray.getInt(i))
        }
        println("Retrieved last predictions: $predictionsList")
        Log.d("DataScreen", "Converted predictions list: $predictionsList")

        predictionsList
    } catch (e: Exception) {
        println("Error retrieving predictions: ${e.message}")
        emptyList()
    }
}
