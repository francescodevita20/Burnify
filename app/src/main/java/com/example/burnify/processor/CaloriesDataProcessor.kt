package com.example.burnify.processor


object CaloriesDataProcessor {

    fun processMeasurements(weight: Int, activityLevel: String, time: Float): Float {
        val met = when (activityLevel.lowercase()) {
            "sedentary" -> 1.2
            "light" -> 2.5
            "moderate" -> 4.0
            "active" -> 6.0
            "very active" -> 8.0
            else -> 1.2 // Default to sedentary if unrecognized
        }

        //TODO: FIX THE MET COMPUTING. (if 1, if 2, ...)

        val caloriesBurned: Float = weight.toFloat() * met.toFloat() * time
        return caloriesBurned
    }
}
