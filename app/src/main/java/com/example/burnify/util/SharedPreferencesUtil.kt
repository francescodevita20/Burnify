package com.example.burnify.util

import android.content.Context
import android.util.Log
import com.example.burnify.viewmodel.Prediction
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

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
 * @param prediction The prediction to add.
 * @param sharedPreferencesName The name of the SharedPreferences.
 * @return Boolean indicating if the save was successful.
 */
@Synchronized
fun addPredictionToSharedPreferences(context: Context, prediction: Prediction, sharedPreferencesName: String): Boolean {
    return try {
        val sharedPreferences = context.getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE)

        // Get current predictions or initialize new list
        val currentPredictions = getLastPredictionsFromSharedPreferences(context, sharedPreferencesName)
        val updatedPredictions = currentPredictions.toMutableList()

        // Add new prediction
        updatedPredictions.add(prediction)

        // Keep only last 5 predictions
        while (updatedPredictions.size > 5) {
            updatedPredictions.removeAt(0)
        }

        // Convert to JSON array and save
        val gson = Gson()
        val predictionsJson = gson.toJson(updatedPredictions)

        val saveSuccess = sharedPreferences.edit()
            .putString("last_predictions", predictionsJson)
            .commit()

        if (saveSuccess) {
            Log.d("SharedPreferences", "Successfully saved predictions: $predictionsJson")
        } else {
            Log.e("SharedPreferences", "Failed to save predictions")
        }

        saveSuccess
    } catch (e: Exception) {
        Log.e("SharedPreferences", "Error saving prediction: ${e.message}")
        false
    }
}

/**
 * Retrieves the last 5 predictions from SharedPreferences.
 * @param context The application context.
 * @param sharedPreferencesName The name of the SharedPreferences.
 * @return A list of the last 5 predictions as `Prediction` objects.
 */
@Synchronized
fun getLastPredictionsFromSharedPreferences(context: Context, sharedPreferencesName: String): List<Prediction> {
    return try {
        val sharedPreferences = context.getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE)
        val predictionsJson = sharedPreferences.getString("last_predictions", null)

        // If no data exists yet, return empty list
        if (predictionsJson == null) {
            Log.d("SharedPreferences", "No predictions found in SharedPreferences")
            return emptyList()
        }

        // Parse JSON array to list of Prediction objects
        val gson = Gson()
        val predictionsList: List<Prediction> = gson.fromJson(predictionsJson, Array<Prediction>::class.java).toList()

        Log.d("SharedPreferences", "Retrieved predictions: $predictionsList")
        predictionsList

    } catch (e: Exception) {
        Log.e("SharedPreferences", "Error retrieving predictions: ${e.message}")
        emptyList()
    }
}
