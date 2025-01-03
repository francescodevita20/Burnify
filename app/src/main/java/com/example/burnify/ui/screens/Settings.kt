package com.example.burnify.ui.screens

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.example.burnify.R
import com.example.burnify.databinding.SettingsScreenBinding
import com.example.burnify.util.getSharedPreferences
import com.example.burnify.util.setSharedPreferences

class Settings : Fragment() {
    private var selectedMode = "Maximum Battery Saving"
    private var weight = ""
    private var height = ""
    private var age = ""
    private var gender = "Male"

    private var _binding: SettingsScreenBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SettingsScreenBinding.inflate(inflater, container, false)

        // Load settings from SharedPreferences
        loadSettings()

        // Set up the Mode CardView and the gender radio buttons
        setupModeSelection()
        setupUserInformation()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadSettings() {
        // Load working mode setting
        val sharedPreferences = requireActivity().getSharedPreferences("settings", Context.MODE_PRIVATE)
        selectedMode = sharedPreferences.getString("workingmode", "Maximum Accuracy") ?: "Maximum Accuracy"

        // Load user data for weight, height, age, and gender
        val userData = requireContext().getSharedPreferences("userdata", Context.MODE_PRIVATE)

        // Retrieve values safely
        weight = userData.getInt("weight", -1).takeIf { it != -1 }?.toString() ?: "Not Set"
        height = userData.getInt("height", -1).takeIf { it != -1 }?.toString() ?: "Not Set"
        age = userData.getInt("age", -1).takeIf { it != -1 }?.toString() ?: "Not Set"
        gender = userData.getString("gender", "Male") ?: "Male"
    }


    private fun setupModeSelection() {


        // Set up the RadioGroup with predefined RadioButtons in XML
        binding.ModeRadioGroup.apply {
            // Ensure the correct radio button is checked based on selectedMode
            when (selectedMode) {
                "Maximum Accuracy" -> binding.modeMaxAccuracyRadioButton.isChecked = true
                "Maximum Battery Saving" -> binding.modesavedModeRadioButton.isChecked = true
            }

            // Listen for mode changes
            setOnCheckedChangeListener { _, checkedId ->
                selectedMode = when (checkedId) {
                    R.id.modeMaxAccuracyRadioButton -> "Maximum Accuracy"
                    R.id.modesavedModeRadioButton -> "Maximum Battery Saving"
                    else -> selectedMode
                }
                // Save selected mode to SharedPreferences
                requireActivity().getSharedPreferences("settings", Context.MODE_PRIVATE)
                    .edit()
                    .putString("workingmode", selectedMode)
                    .apply()
            }
        }
    }


    private fun setupUserInformation() {
        binding.apply {
            // Set up TextWatchers
            weightEditText.setupTextWatcher("weight")
            heightEditText.setupTextWatcher("height")
            ageEditText.setupTextWatcher("age")

            // Set initial values
            weightEditText.setText(weight)
            heightEditText.setText(height)
            ageEditText.setText(age)

            // Set up gender radio group
            genderRadioGroup.setOnCheckedChangeListener { _, checkedId ->
                gender = when (checkedId) {
                    R.id.genderMaleRadioButton -> "Male"
                    R.id.genderFemaleRadioButton -> "Female"
                    else -> gender
                }
                updateUserData("gender", gender)
            }

            // Set initial gender selection
            if (gender == "Male") {
                genderMaleRadioButton.isChecked = true
            } else {
                genderFemaleRadioButton.isChecked = true
            }
        }
    }

    private fun EditText.setupTextWatcher(key: String) {
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateUserData(key, s?.toString() ?: "")
            }
        })
    }

    private fun updateUserData(key: String, value: String) {
        val currentData = getSharedPreferences(requireContext(), "userdata", "user_data_key")?.toMutableMap() ?: mutableMapOf()
        currentData[key] = value
        setSharedPreferences(requireContext(), currentData, "userdata", "user_data_key")
    }

}
