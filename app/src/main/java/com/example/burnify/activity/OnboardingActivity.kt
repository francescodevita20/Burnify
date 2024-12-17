package com.example.burnify.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.burnify.R
import com.example.burnify.util.setSharedPreferences

class OnboardingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        val weightEditText = findViewById<EditText>(R.id.weightEditText)
        val heightEditText = findViewById<EditText>(R.id.heightEditText)
        val ageEditText = findViewById<EditText>(R.id.ageEditText)
        val saveButton = findViewById<Button>(R.id.saveButton)
        val genderRadioGroup = findViewById<RadioGroup>(R.id.genderRadioGroup)

        saveButton.setOnClickListener {
            // Get user inputs and provide default values if missing
            val weight = weightEditText.text.toString().toIntOrNull() ?: 70
            val height = heightEditText.text.toString().toIntOrNull() ?: 165
            val age = ageEditText.text.toString().toIntOrNull() ?: 25

            // Get selected gender
            val selectedGenderId = genderRadioGroup.checkedRadioButtonId
            val gender = if (selectedGenderId == R.id.maleRadioButton) "Male" else "Female"

            // Log the values being saved for debugging
            println("Saving user data: weight=$weight, height=$height, age=$age, gender=$gender")

            // Save the user data to SharedPreferences
            val userData = mapOf(
                "weight" to weight,
                "height" to height,
                "age" to age,
                "gender" to gender
            )

            // Save the data to SharedPreferences using the utility function
            setSharedPreferences(this, userData, "userdata", "user_data_key")

            // Confirm the save action with a Toast (optional for debugging)
            Toast.makeText(this, "User data saved!", Toast.LENGTH_SHORT).show()

            // Log for debugging purposes
            println("User data saved successfully in SharedPreferences.")

            // Add delay to allow Toast to show
            Handler(mainLooper).postDelayed({
                // Log that we're transitioning to MainActivity
                println("Transitioning to MainActivity...")

                // Log that OnboardingActivity is finishing
                println("Finishing OnboardingActivity.")

                // Finish the current activity to remove it from the stack
                finish()

            }, 1000) // 1 second delay for smooth transition
        }
    }
}
