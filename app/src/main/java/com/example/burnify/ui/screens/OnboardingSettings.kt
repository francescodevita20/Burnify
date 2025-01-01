package com.example.burnify.ui.screens

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.burnify.R
import com.example.burnify.databinding.OnboardingSettingsBinding
import com.example.burnify.util.setSharedPreferences
import android.widget.Toast
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat.getSystemService
import com.example.burnify.activity.MainActivity
import android.content.Context
import android.view.inputmethod.InputMethodManager

class OnboardingSettings : Fragment() {
    private var _binding: OnboardingSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = OnboardingSettingsBinding.inflate(inflater, container, false)

        binding.saveButton.setOnClickListener {
            saveUserData()
            // Hide the keyboard after the data is saved
            val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            val view = requireActivity().window.decorView
            imm.hideSoftInputFromWindow(view.windowToken, 0)

        }

        return binding.root
    }

    private fun saveUserData() {
        val weight = binding.weightEditText.text.toString().toIntOrNull() ?: 70
        val height = binding.heightEditText.text.toString().toIntOrNull() ?: 165
        val age = binding.ageEditText.text.toString().toIntOrNull() ?: 25

        val selectedGenderId = binding.genderRadioGroup.checkedRadioButtonId
        val gender = when (selectedGenderId) {
            R.id.maleRadioButton -> "Male"
            else -> "Female"
        }

        val userData = mapOf(
            "weight" to weight,
            "height" to height,
            "age" to age,
            "gender" to gender
        )

        setSharedPreferences(requireContext(), userData, "userdata", "user_data_key")
        Toast.makeText(requireContext(), "User data saved!", Toast.LENGTH_SHORT).show()

        Handler(Looper.getMainLooper()).postDelayed({
            (activity as? MainActivity)?.let { mainActivity ->
                (activity as? MainActivity)?.switchToMainContent()
            }
        }, 1000)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}




