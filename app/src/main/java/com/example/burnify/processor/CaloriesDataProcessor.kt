package com.example.burnify.processor


class CaloriesDataProcessor {

    fun processMeasurements(weight: Double, activityLevel: String, time: Double): Int {
        val met = when (activityLevel.lowercase()) {
            "sedentary" -> 1.2
            "light" -> 2.5
            "moderate" -> 4.0
            "active" -> 6.0
            "very active" -> 8.0
            else -> 1.2 // Default to sedentary if unrecognized
        }
        val caloriesBurned: Int = (weight * met * time).toInt()
        return caloriesBurned
    }
}
