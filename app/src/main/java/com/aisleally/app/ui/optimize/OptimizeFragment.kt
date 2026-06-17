package com.aisleally.app.ui.optimize

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.aisleally.app.R
import com.aisleally.app.databinding.FragmentOptimizeBinding
import com.aisleally.app.data.Optimizer
import com.aisleally.app.model.CategoryLimit
import com.aisleally.app.model.OptimizationConstraints
import com.aisleally.app.ui.SharedViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView

class OptimizeFragment : Fragment() {

    private var _binding: FragmentOptimizeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOptimizeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Pre-fill from generated targets
        viewModel.nutritionTargets.value?.let { t ->
            if (t.dailyCalories > 0) {
                binding.etMinCalories.setText(t.minCalories.toString())
                binding.etMaxCalories.setText(t.maxCalories.toString())
                binding.etProteinTarget.setText(t.protein.toString())
                binding.etFiberTarget.setText(t.fiber.toString())
                binding.etFatTarget.setText(t.fat.toString())
            }
        }

        // Pre-fill from saved constraints
        viewModel.constraints.value?.let { c ->
            if (c.budget > 0) binding.etBudget.setText(c.budget.toString())
            if (c.days   > 0) binding.etDays.setText(c.days.toString())
        }

        // Observe optimization result
        viewModel.optimizationResult.observe(viewLifecycleOwner) { result ->
            if (result == null) return@observe

            when (result) {
                is Optimizer.OptimizationResult.Failure -> {
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                    viewModel.clearOptimizationResult()
                }
                is Optimizer.OptimizationResult.Success -> {
                    // Only perform the redirect if it hasn't been navigated yet
                    if (!hasNavigatedToResults) {
                        hasNavigatedToResults = true
                        activity?.findViewById<BottomNavigationView>(R.id.bottom_nav)
                            ?.selectedItemId = R.id.nav_results
                    }
                }
            }
        }

        // Update targets when days changes
        binding.btnUpdateTargets.setOnClickListener {
            val days = binding.etDays.text.toString().toIntOrNull()
            if (days == null || days <= 0) {
                Toast.makeText(requireContext(), "Please enter valid days.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val t = viewModel.nutritionTargets.value ?: return@setOnClickListener
            if (t.dailyCalories == 0) {
                Toast.makeText(requireContext(), "Generate nutrition targets in Profile first.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            binding.etMinCalories.setText((t.minCalories * days).toString())
            binding.etMaxCalories.setText((t.maxCalories * days).toString())
            binding.etProteinTarget.setText((t.protein * days).toString())
            binding.etFiberTarget.setText((t.fiber * days).toString())
            binding.etFatTarget.setText((t.fat * days).toString())
        }

        // Run optimization
        binding.btnOptimize.setOnClickListener {
            val budget = binding.etBudget.text.toString().toDoubleOrNull()
            val days   = binding.etDays.text.toString().toIntOrNull()

            if (budget == null || days == null || days <= 0) {
                Toast.makeText(requireContext(), "Please enter valid budget and days.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Reset the navigation flag because we are running a brand-new optimization
            hasNavigatedToResults = false

            val constraints = OptimizationConstraints(
                budget        = budget,
                days          = days,
                minCalories   = binding.etMinCalories.text.toString().toDoubleOrNull()   ?: 0.0,
                maxCalories   = binding.etMaxCalories.text.toString().toDoubleOrNull()   ?: 0.0,
                proteinTarget = binding.etProteinTarget.text.toString().toDoubleOrNull() ?: 0.0,
                fiberTarget   = binding.etFiberTarget.text.toString().toDoubleOrNull()   ?: 0.0,
                fatTarget     = binding.etFatTarget.text.toString().toDoubleOrNull()     ?: 0.0,
                categoryLimits = mapOf(
                    "protein"       to CategoryLimit(
                        binding.etProteinMin.text.toString().toIntOrNull() ?: 0,
                        binding.etProteinMax.text.toString().toIntOrNull() ?: Int.MAX_VALUE
                    ),
                    "vegetables"    to CategoryLimit(
                        binding.etVegetableMin.text.toString().toIntOrNull() ?: 0,
                        binding.etVegetableMax.text.toString().toIntOrNull() ?: Int.MAX_VALUE
                    ),
                    "carbohydrates" to CategoryLimit(
                        binding.etCarbMin.text.toString().toIntOrNull() ?: 0,
                        binding.etCarbMax.text.toString().toIntOrNull() ?: Int.MAX_VALUE
                    ),
                    "fats"          to CategoryLimit(
                        binding.etFatMin.text.toString().toIntOrNull() ?: 0,
                        binding.etFatMax.text.toString().toIntOrNull() ?: Int.MAX_VALUE
                    )
                )
            )

            viewModel.runOptimization(constraints)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        // Keeps track of navigation state across fragment recreation
        private var hasNavigatedToResults = false
    }
}