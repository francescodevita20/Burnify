package com.example.burnify.processor

/**
 * Object that calculates the number of calories burned based on the user's weight, activity level, and time spent.
 */
object CaloriesDataProcessor {

    /**
     * Processes the given measurements and calculates the number of calories burned.
     *
     * @param weight The user's weight in kilograms.
     * @param activityLevel The user's level of physical activity (e.g., "sedentary", "light", "moderate", "active", "very active").
     * @param time The duration of the activity in hours.
     * @return The estimated number of calories burned.
     */
    fun processMeasurements(weight: Int, activityLevel: String, time: Float): Float {
        // MET (Metabolic Equivalent of Task) values based on the activity level
        val met = when (activityLevel.lowercase()) {
            "sedentary" -> 1.2  // Little to no exercise
            "light" -> 2.5      // Light exercise (e.g., walking)
            "moderate" -> 4.0   // Moderate exercise (e.g., jogging)
            "active" -> 6.0     // Active exercise (e.g., running)
            "very active" -> 8.0 // Very active exercise (e.g., intense workout)
            else -> 1.2         // Default to sedentary if unrecognized activity level
        }

        // TODO: Improve MET computation (e.g., add more specific activity levels or use more accurate values)

        // Calculate the number of calories burned using the formula:
        // Calories burned = weight (kg) * MET * time (hours)
        val caloriesBurned: Float = weight.toFloat() * met.toFloat() * time

        return caloriesBurned
    }
}