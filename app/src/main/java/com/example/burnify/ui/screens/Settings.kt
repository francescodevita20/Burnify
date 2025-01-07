package com.example.burnify.ui.screens

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.burnify.R
import com.example.burnify.activity.MainActivity
import com.example.burnify.databinding.SettingsScreenBinding
import com.example.burnify.util.getSharedPreferences
import com.example.burnify.util.setSharedPreferences

class Settings : Fragment() {
    private var _binding: SettingsScreenBinding? = null
    private val binding get() = _binding!!

    private val userData = UserSettings()
    private class UserSettings {
        var selectedMode: String = "Maximum Battery Saving"
        var weight: String = ""
        var height: String = ""
        var age: String = ""
        var gender: String = "Male"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SettingsScreenBinding.inflate(inflater, container, false)
        initializeSettings()
        return binding.root
    }

    private fun initializeSettings() {
        loadSettings()
        setupModeSelection()
        setupUserInformation()
        setupUpdateButton()
        stopApp()
    }

    private fun loadSettings() {
        requireActivity().getSharedPreferences("settings", Context.MODE_PRIVATE).apply {
            userData.selectedMode = getString("workingmode", "Maximum Accuracy") ?: "Maximum Accuracy"
        }

        requireContext().getSharedPreferences("userdata", Context.MODE_PRIVATE).apply {
            userData.weight = getInt("weight", -1).takeIf { it != -1 }?.toString() ?: "Not Set"
            userData.height = getInt("height", -1).takeIf { it != -1 }?.toString() ?: "Not Set"
            userData.age = getInt("age", -1).takeIf { it != -1 }?.toString() ?: "Not Set"
            userData.gender = getString("gender", "Male") ?: "Male"
        }
    }

    private fun setupModeSelection() {
        binding.ModeRadioGroup.apply {
            when (userData.selectedMode) {
                "Maximum Accuracy" -> binding.modeMaxAccuracyRadioButton.isChecked = true
                "Maximum Battery Saving" -> binding.modesavedModeRadioButton.isChecked = true
            }

            setOnCheckedChangeListener { _, checkedId ->
                userData.selectedMode = when (checkedId) {
                    R.id.modeMaxAccuracyRadioButton -> "Maximum Accuracy"
                    R.id.modesavedModeRadioButton -> "Maximum Battery Saving"
                    else -> userData.selectedMode
                }
                saveModeSettings()
            }
        }
    }

    private fun saveModeSettings() {
        requireActivity().getSharedPreferences("settings", Context.MODE_PRIVATE)
            .edit()
            .putString("workingmode", userData.selectedMode)
            .apply()
    }

    private fun setupUserInformation() {
        binding.apply {
            weightEditText.apply {
                setText(userData.weight)
                setupTextWatcher("weight")
            }
            heightEditText.apply {
                setText(userData.height)
                setupTextWatcher("height")
            }
            ageEditText.apply {
                setText(userData.age)
                setupTextWatcher("age")
            }

            genderRadioGroup.apply {
                check(if (userData.gender == "Male") R.id.genderMaleRadioButton else R.id.genderFemaleRadioButton)
                setOnCheckedChangeListener { _, checkedId ->
                    userData.gender = when (checkedId) {
                        R.id.genderMaleRadioButton -> "Male"
                        R.id.genderFemaleRadioButton -> "Female"
                        else -> userData.gender
                    }
                    updateUserData("gender", userData.gender)
                }
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

    private fun setupUpdateButton() {
        binding.updateButton.setOnClickListener {
            if (validateUserInput()) {
                saveUserData()
                hideKeyboard()
            }
        }
    }

    private fun validateUserInput(): Boolean {
        val weight = binding.weightEditText.text.toString()
        val height = binding.heightEditText.text.toString()
        val age = binding.ageEditText.text.toString()

        return when {
            weight.isEmpty() || height.isEmpty() || age.isEmpty() -> {
                showToast("Please fill in all fields")
                false
            }
            !validateWeight(weight.toIntOrNull()) -> false
            !validateHeight(height.toIntOrNull()) -> false
            !validateAge(age.toIntOrNull()) -> false
            binding.genderRadioGroup.checkedRadioButtonId == -1 -> {
                showToast("Please select your gender")
                false
            }
            binding.ModeRadioGroup.checkedRadioButtonId == -1 -> {
                showToast("Please select mode")
                false
            }
            else -> true
        }
    }

    private fun validateWeight(weight: Int?) = when {
        weight == null || weight < 30 || weight > 300 -> {
            showToast("Please enter a valid weight (30-300 kg)")
            false
        }
        else -> true
    }

    private fun validateHeight(height: Int?) = when {
        height == null || height < 100 || height > 250 -> {
            showToast("Please enter a valid height (100-250 cm)")
            false
        }
        else -> true
    }

    private fun validateAge(age: Int?) = when {
        age == null || age < 13 || age > 120 -> {
            showToast("Please enter a valid age (13-120 years)")
            false
        }
        else -> true
    }

    private fun saveUserData() {
        try {
            requireContext().getSharedPreferences("userdata", Context.MODE_PRIVATE)
                .edit()
                .putInt("weight", binding.weightEditText.text.toString().toInt())
                .putInt("height", binding.heightEditText.text.toString().toInt())
                .putInt("age", binding.ageEditText.text.toString().toInt())
                .putString("gender", userData.gender)
                .apply()

            showToast("Settings updated successfully")
        } catch (e: Exception) {
            showToast("Error saving settings. Please try again.")
        }
    }

    private fun updateUserData(key: String, value: String) {
        getSharedPreferences(requireContext(), "userdata", "user_data_key")?.toMutableMap()
            ?.also { currentData ->
                currentData[key] = value
                setSharedPreferences(requireContext(), currentData, "userdata", "user_data_key")
            }
    }

    private fun stopApp() {
        binding.stop.setOnClickListener {
            val activity = requireActivity() as MainActivity  // Ensure the activity is of type MainActivity
            activity.stopUnifiedSensorService()  // Stop the UnifiedSensorService
            activity.finish()
        }
    }



    private fun hideKeyboard() {
        try {
            val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(requireView().windowToken, 0)
        } catch (e: Exception) {
            // Keyboard hiding failed silently
        }
    }



    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}