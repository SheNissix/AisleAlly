package com.aisleally.app.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.aisleally.app.R
import com.aisleally.app.data.ProfileRepository
import com.aisleally.app.databinding.FragmentProfileBinding
import com.aisleally.app.model.UserProfile
import com.aisleally.app.ui.SharedViewModel

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Populate spinners
        val sexOptions = resources.getStringArray(R.array.sex_options)
        binding.spinnerSex.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_item,
            sexOptions
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        val activityOptions = resources.getStringArray(R.array.activity_options)
        binding.spinnerActivity.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_item,
            activityOptions
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        binding.ivActivityInfo.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Activity Levels")
                .setMessage("• Sedentary: Little or no exercise.\n• Lightly Active: Light exercise 1-3 days/week.\n• Moderately Active: Moderate exercise 3-5 days/week.\n• Very Active: Heavy exercise 6-7 days/week.\n• Super Active: Very heavy physical job or training.")
                .setPositiveButton("Got It", null)
                .show()
        }

        viewModel.nutritionTargets.observe(viewLifecycleOwner) { targets ->
            if (targets.dailyCalories > 0) {
                binding.tvDailyCalories.text   = "${targets.dailyCalories} kcal"
                binding.tvMinCalories.text     = "${targets.minCalories} kcal"
                binding.tvMaxCalories.text     = "${targets.maxCalories} kcal"
                binding.tvProteinTarget.text   = "${targets.protein} g"
                binding.tvFiberTarget.text     = "${targets.fiber} g"
                binding.tvFatTarget.text       = "${targets.fat} g"
            }
        }

        // Load saved profile if available
        ProfileRepository.profile?.let { profile ->
            binding.etAge.setText(profile.age.toString())
            binding.etWeight.setText(profile.profileWeightText())
            binding.etHeight.setText(profile.profileHeightText())
            
            val sexIndex = sexOptions.indexOf(profile.sex)
            if (sexIndex >= 0) binding.spinnerSex.setSelection(sexIndex)
            
            val actIndex = activityOptions.indexOf(profile.activity)
            if (actIndex >= 0) binding.spinnerActivity.setSelection(actIndex)
            
            // Automatically generate targets on initial load if we have a saved profile
            // Only generate if we don't already have valid targets in the viewModel
            if (viewModel.nutritionTargets.value?.dailyCalories == 0) {
                viewModel.generateTargets(profile.age, profile.weight, profile.height, profile.sex, profile.activity)
            }
        }

        binding.btnGenerateTargets.setOnClickListener {
            val age    = binding.etAge.text.toString().toIntOrNull()
            val weight = binding.etWeight.text.toString().toDoubleOrNull()
            val height = binding.etHeight.text.toString().toDoubleOrNull()

            if (age == null || weight == null || height == null) {
                Toast.makeText(requireContext(), "Please fill in all fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (age <= 0 || weight <= 0.0 || height <= 0.0) {
                Toast.makeText(requireContext(), "Please enter positive values for age, weight, and height.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val sex      = binding.spinnerSex.selectedItem.toString()
            val activity = binding.spinnerActivity.selectedItem.toString()

            // Save to repository
            ProfileRepository.saveProfile(UserProfile(age, weight, height, sex, activity))

            viewModel.generateTargets(age, weight, height, sex, activity)
        }
    }
    
    // Extension functions to display doubles cleanly (e.g. "70" instead of "70.0")
    private fun UserProfile.profileWeightText(): String {
        return if (weight % 1.0 == 0.0) weight.toInt().toString() else weight.toString()
    }

    private fun UserProfile.profileHeightText(): String {
        return if (height % 1.0 == 0.0) height.toInt().toString() else height.toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
