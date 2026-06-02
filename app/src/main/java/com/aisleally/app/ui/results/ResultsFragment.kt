package com.aisleally.app.ui.results

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.aisleally.app.R
import com.aisleally.app.data.Optimizer
import com.aisleally.app.databinding.FragmentResultsBinding
import com.aisleally.app.model.SolutionItem
import com.aisleally.app.ui.SharedViewModel

class ResultsFragment : Fragment() {

    private var _binding: FragmentResultsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResultsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.optimizationResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Optimizer.OptimizationResult.Success -> {
                    renderResults(result.solution)
                }
                is Optimizer.OptimizationResult.Failure -> {
                    binding.containerSelectedItems.removeAllViews()
                    binding.tvTotalCost.text = "--"
                    binding.tvAvgCalories.text = "--"
                    binding.tvAvgProtein.text = "--"
                    binding.tvAvgFiber.text = "--"
                    binding.tvAvgFat.text = "--"
                    binding.tvItemCount.text = "--"
                }
                else -> {}
            }
        }
    }

    private fun renderResults(solution: List<SolutionItem>) {
        val c = viewModel.constraints.value ?: return

        var totalCost     = 0.0
        var totalCalories = 0.0
        var totalProtein  = 0.0
        var totalFiber    = 0.0
        var totalFat      = 0.0
        var totalItems    = 0

        binding.containerSelectedItems.removeAllViews()

        solution.forEach { item ->
            val itemCost     = item.price    * item.quantity
            val itemCalories = item.calories * item.quantity
            val itemProtein  = item.protein  * item.quantity
            val itemFiber    = item.fiber    * item.quantity
            val itemFat      = item.fat      * item.quantity

            totalCost     += itemCost
            totalCalories += itemCalories
            totalProtein  += itemProtein
            totalFiber    += itemFiber
            totalFat      += itemFat
            totalItems    += item.quantity

            addItemRow(item, itemCost)
        }

        binding.tvTotalCost.text     = "₱${totalCost.toInt()} / ₱${c.budget.toInt()}"
        binding.tvAvgCalories.text   = "${totalCalories.toInt()} / ${c.minCalories.toInt()}–${c.maxCalories.toInt()} kcal"
        binding.tvAvgProtein.text    = "${totalProtein.toInt()}g / ${c.proteinTarget.toInt()}g"
        binding.tvAvgFiber.text      = "${totalFiber.toInt()}g / ${c.fiberTarget.toInt()}g"
        binding.tvAvgFat.text        = "${totalFat.toInt()}g / ${c.fatTarget.toInt()}g"
        binding.tvItemCount.text     = totalItems.toString()
    }

    private fun addItemRow(item: SolutionItem, itemCost: Double) {
        val row = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 24, 0, 24)
        }

        val infoLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val nameView = TextView(requireContext()).apply {
            text = "${item.quantity}x ${item.name}"
            setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyLarge)
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(0xFF000000.toInt())
        }

        val detailView = TextView(requireContext()).apply {
            text = "₱${item.price.toInt()} each · ${item.calories.toInt()} kcal each"
            setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
        }

        infoLayout.addView(nameView)
        infoLayout.addView(detailView)

        val priceView = TextView(requireContext()).apply {
            text = "₱${itemCost.toInt()}"
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(ContextCompat.getColor(requireContext(), R.color.green_primary))
        }

        row.addView(infoLayout)
        row.addView(priceView)
        binding.containerSelectedItems.addView(row)

        // Divider
        val divider = View(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1
            ).also { it.topMargin = 0 }
            setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.divider))
        }
        binding.containerSelectedItems.addView(divider)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
