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


/*
package com.example.burnify.processor

/**
 * Object that calculates the number of calories burned based on user-specific factors, activity level, and time spent.
 */
object CaloriesDataProcessor {

    /**
     * Processes the given measurements and calculates the number of calories burned.
     *
     * @param weight The user's weight in kilograms.
     * @param activityLevel The user's level of physical activity (e.g., "walking", "running").
     * @param time The duration of the activity in hours.
     * @param age (Optional) The user's age for metabolism adjustment.
     * @param gender (Optional) The user's gender ("male" or "female").
     * @param fitnessLevel (Optional) The user's fitness level ("beginner", "intermediate", "advanced").
     * @return The estimated number of calories burned.
     */
    fun processMeasurements(
        weight: Int,
        activityLevel: String,
        time: Float,
        age: Int? = null,
        gender: String? = null,
        fitnessLevel: String? = null
    ): Float {
        // MET values for common activities
        val baseMet = when (activityLevel.lowercase()) {
            "sedentary" -> 1.2  // Little to no exercise
            "light" -> 2.5      // Light exercise (e.g., walking)
            "moderate" -> 4.0   // Moderate exercise (e.g., jogging)
            "active" -> 6.0     // Active exercise (e.g., running)
            "very active" -> 8.0 // Intense exercise
            "walking" -> 3.5
            "running" -> 7.0
            "cycling" -> 6.8
            "swimming" -> 9.8
            "weightlifting" -> 6.0
            else -> throw IllegalArgumentException("Invalid activity level: $activityLevel")
        }

        // Adjust MET value for user-specific factors
        val adjustedMet = adjustMetForUser(baseMet, age, gender, fitnessLevel)

        // Calculate calories burned
        return weight.toFloat() * adjustedMet * time
    }

    /**
     * Adjusts the MET value based on user-specific factors such as age, gender, and fitness level.
     */
    private fun adjustMetForUser(
        met: Float,
        age: Int?,
        gender: String?,
        fitnessLevel: String?
    ): Float {
        var adjustedMet = met

        // Adjust for age (reduce MET slightly for older users)
        if (age != null && age > 40) {
            adjustedMet *= 0.9f
        }

        // Adjust for gender (slightly lower MET for females)
        if (gender != null && gender.lowercase() == "female") {
            adjustedMet *= 0.95f
        }

        // Adjust for fitness level
        when (fitnessLevel?.lowercase()) {
            "beginner" -> adjustedMet *= 1.1f  // Higher MET for beginners (exerting more energy)
            "intermediate" -> adjustedMet *= 1.0f
            "advanced" -> adjustedMet *= 0.9f  // Lower MET for advanced users (more efficient energy use)
        }

        return adjustedMet
    }

    /**
     * Dynamically calculates the total calories burned based on a stream of detected activities and their durations.
     *
     * @param weight The user's weight in kilograms.
     * @param activityStream A list of activity-durations pairs (e.g., "walking" to 0.5 hours).
     * @param age (Optional) The user's age for metabolism adjustment.
     * @param gender (Optional) The user's gender ("male" or "female").
     * @param fitnessLevel (Optional) The user's fitness level ("beginner", "intermediate", "advanced").
     * @return The total estimated calories burned.
     */
    fun processDynamicCalories(
        weight: Int,
        activityStream: List<Pair<String, Float>>,
        age: Int? = null,
        gender: String? = null,
        fitnessLevel: String? = null
    ): Float {
        var totalCalories = 0f

        for (activity in activityStream) {
            val (activityLevel, duration) = activity
            totalCalories += processMeasurements(weight, activityLevel, duration, age, gender, fitnessLevel)
        }

        return totalCalories
    }

    /**
     * Unit test utility function to validate calorie calculations with sample inputs.
     * (This is for debugging purposes; in a real app, use dedicated testing frameworks.)
     */
    fun testCaloriesProcessor() {
        val sampleActivities = listOf(
            "walking" to 0.5f,   // 30 minutes of walking
            "running" to 0.2f,   // 12 minutes of running
            "cycling" to 0.4f    // 24 minutes of cycling
        )

        val weight = 70  // Example: 70kg user
        val age = 45     // Example: 45 years old
        val gender = "female"
        val fitnessLevel = "intermediate"

        val totalCalories = processDynamicCalories(weight, sampleActivities, age, gender, fitnessLevel)
        println("Total Calories Burned: $totalCalories")
    }
}

 */