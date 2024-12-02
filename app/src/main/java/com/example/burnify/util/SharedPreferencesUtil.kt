package com.example.burnify.util

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

fun setSharedPreferences(context: Context, newMap: Map<String, Any>, sharedPreferencesName: String) {
    try {
        val sharedPreferences = context.getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val gson = GsonBuilder().serializeSpecialFloatingPointValues().create()
        val updatedJson = gson.toJson(newMap)

        editor.putString(sharedPreferencesName, updatedJson)
        editor.apply()

        println("Map saved successfully in SharedPreferences.")
    } catch (e: Exception) {
        println("Error saving map: ${e.message}")
    }
}

fun getSharedPreferences(context: Context, sharedPreferencesName: String): Map<String, Any>? {
    return try {
        val sharedPreferences = context.getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE)
        val json = sharedPreferences.getString(sharedPreferencesName, null)

        if (json.isNullOrEmpty()) {
            println("No data found in SharedPreferences.")
            return null
        } else {
            val gson = Gson()
            val type = object : TypeToken<Map<String, Any>>() {}.type
            val map: Map<String, Any> = gson.fromJson(json, type)
            println("SharedPreferences retrieved successfully.")
            map
        }
    } catch (e: Exception) {
        println("Error retrieving data: ${e.message}")
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
