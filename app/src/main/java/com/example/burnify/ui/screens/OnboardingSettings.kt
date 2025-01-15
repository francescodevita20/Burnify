package com.example.burnify.ui.screens

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.burnify.R
import com.example.burnify.activity.MainActivity
import com.example.burnify.databinding.OnboardingSettingsBinding

class OnboardingSettings : Fragment() {
    private var _binding: OnboardingSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = OnboardingSettingsBinding.inflate(inflater, container, false)

        setupSaveButton()
        return binding.root
    }

    private fun setupSaveButton() {
        binding.saveButton.setOnClickListener {
            if (validateInput()) {
                hideKeyboard()
                saveUserData()
            }
        }
    }

    private fun validateInput(): Boolean {
        val weight = binding.weightEditText.text.toString()
        val height = binding.heightEditText.text.toString()
        val age = binding.ageEditText.text.toString()

        // Check if any field is empty
        if (weight.isEmpty() || height.isEmpty() || age.isEmpty()) {
            showToast("Please fill in all fields")
            return false
        }

        // Validate weight (reasonable range: 30-300 kg)
        val weightValue = weight.toIntOrNull()
        if (weightValue == null || weightValue < 30 || weightValue > 300) {
            showToast("Please enter a valid weight (30-300 kg)")
            return false
        }

        // Validate height (reasonable range: 100-250 cm)
        val heightValue = height.toIntOrNull()
        if (heightValue == null || heightValue < 100 || heightValue > 250) {
            showToast("Please enter a valid height (100-250 cm)")
            return false
        }

        // Validate age (reasonable range: 13-120 years)
        val ageValue = age.toIntOrNull()
        if (ageValue == null || ageValue < 13 || ageValue > 120) {
            showToast("Please enter a valid age (13-120 years)")
            return false
        }

        // Check if gender is selected
        if (binding.genderRadioGroup.checkedRadioButtonId == -1) {
            showToast("Please select your gender")
            return false
        }

        return true
    }

    private fun hideKeyboard() {
        try {
            val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(requireView().windowToken, 0)
        } catch (e: Exception) {
            // Log error but continue with saving
        }
    }

    private fun saveUserData() {
        try {
            // Get values (already validated)
            val weight = binding.weightEditText.text.toString().toInt()
            val height = binding.heightEditText.text.toString().toInt()
            val age = binding.ageEditText.text.toString().toInt()
            val gender = when (binding.genderRadioGroup.checkedRadioButtonId) {
                R.id.maleRadioButton -> "Male"
                R.id.femaleRadioButton -> "Female"
                else -> throw IllegalStateException("Gender not selected")
            }

            // Save to SharedPreferences
            requireContext().getSharedPreferences("userdata", Context.MODE_PRIVATE)
                .edit()
                .putInt("weight", weight)
                .putInt("height", height)
                .putInt("age", age)
                .putString("gender", gender)
                .apply()

            showToast("User data saved successfully!")

            // Switch to main content after short delay
            Handler(Looper.getMainLooper()).postDelayed({
                (activity as? MainActivity)?.switchToMainContent()
            }, 500) // Reduced delay to 500ms for better UX

        } catch (e: Exception) {
            showToast("Error saving data. Please try again.")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}