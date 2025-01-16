package com.example.burnify.util

import android.content.Context
import android.util.Log
import com.example.burnify.viewmodel.Prediction
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

// Save a map of data to SharedPreferences
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

// Retrieve a map from SharedPreferences
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

@Synchronized
fun addPredictionToSharedPreferences(context: Context, prediction: Prediction, sharedPreferencesName: String): Boolean {
    return try {
        val sharedPreferences = context.getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE)

        // Get current predictions or initialize new list
        val currentPredictions = getTodayPredictionsFromSharedPreferences(context, sharedPreferencesName)
        val updatedPredictions = currentPredictions.toMutableList()

        // Add new prediction if it's different from the last one
        if (updatedPredictions.isEmpty() || updatedPredictions.last().label != prediction.label) {
            updatedPredictions.add(prediction)
        }

        // Limit the number of predictions to 10
        val limitedPredictions = updatedPredictions.takeLast(10)

        // Convert to JSON array and save
        val gson = Gson()
        val predictionsJson = gson.toJson(limitedPredictions)

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
 * Retrieves the predictions for today from SharedPreferences.
 * @param context The application context.
 * @param sharedPreferencesName The name of the SharedPreferences.
 * @return A list of predictions for today as `Prediction` objects.
 */
fun getTodayPredictionsFromSharedPreferences(context: Context, sharedPreferencesName: String): List<Prediction> {
    return try {
        val sharedPreferences = context.getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE)
        val predictionsJson = sharedPreferences.getString("last_predictions", null)

        if (predictionsJson == null || predictionsJson.isEmpty()) {
            // If no data exists, return an empty list or fetch all predictions for today
            Log.d("SharedPreferences", "No predictions found, fetching all for today...")
            return emptyList() // Replace this with logic to fetch today's predictions
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

/**
 * Clears the predictions from the previous day from SharedPreferences (useful for daily reset).
 */
fun clearSharedPreferencesForNewDay(context: Context, sharedPreferencesName: String) {
    val sharedPreferences = context.getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()

    // Clear the stored predictions for the new day
    editor.remove("last_predictions")
    editor.apply()

    Log.d("SharedPreferences", "Cleared previous day's predictions.")
}
