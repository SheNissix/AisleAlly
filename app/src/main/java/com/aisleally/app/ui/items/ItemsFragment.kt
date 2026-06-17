package com.aisleally.app.ui.items

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.aisleally.app.R
import com.aisleally.app.databinding.FragmentItemsBinding
import com.aisleally.app.model.GroceryItem
import com.aisleally.app.ui.SharedViewModel

class ItemsFragment : Fragment() {

    private var _binding: FragmentItemsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SharedViewModel by activityViewModels()

    private lateinit var adapter: ItemsAdapter
    private var editingItemId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentItemsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ItemsAdapter(
            onEditClick = { item -> populateEditForm(item) },
            onMaxQtyChanged = { id, qty -> viewModel.updateMaxQuantity(id, qty) },
            onForcedQtyChanged = { id, qty ->
                val error = viewModel.updateForcedQuantity(id, qty)
                if (error != null) Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            },
            onDeleteClick = { id ->
                viewModel.deleteItem(id)
                Toast.makeText(requireContext(), "Item removed.", Toast.LENGTH_SHORT).show()
            }
        )

        binding.recyclerItems.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerItems.adapter = adapter

        viewModel.items.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
        }

        binding.spinnerCategory.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_item,
            resources.getStringArray(R.array.item_categories)
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        binding.btnSaveItem.setOnClickListener { saveItem() }
    }

    private fun saveItem() {
        val name     = binding.etItemName.text.toString().trim()
        val price    = binding.etItemPrice.text.toString().toDoubleOrNull()
        val calories = binding.etItemCalories.text.toString().toDoubleOrNull()
        val protein  = binding.etItemProtein.text.toString().toDoubleOrNull()
        val fiber    = binding.etItemFiber.text.toString().toDoubleOrNull()
        val fat      = binding.etItemFat.text.toString().toDoubleOrNull()

        if (name.isEmpty() || price == null || calories == null ||
            protein == null || fiber == null || fat == null) {
            Toast.makeText(requireContext(), "Please fill in all fields.", Toast.LENGTH_SHORT).show()
            return
        }

        if (price <= 0.0 || calories < 0.0 || protein < 0.0 || fiber < 0.0 || fat < 0.0) {
            Toast.makeText(requireContext(), "Price must be greater than 0. Calories and macros must be non-negative.", Toast.LENGTH_SHORT).show()
            return
        }

        val id = editingItemId ?: System.currentTimeMillis().toString()
        val category = binding.spinnerCategory.selectedItem.toString()

        val item = GroceryItem(
            id       = id,
            name     = name,
            category = category,
            price    = price,
            calories = calories,
            protein  = protein,
            fiber    = fiber,
            fat      = fat
        )

        viewModel.addOrUpdateItem(item)
        editingItemId = null
        clearForm()
        Toast.makeText(requireContext(), "Item saved.", Toast.LENGTH_SHORT).show()
    }

    private fun populateEditForm(item: GroceryItem) {
        editingItemId = item.id
        binding.etItemName.setText(item.name)
        binding.etItemPrice.setText(item.price.toString())
        binding.etItemCalories.setText(item.calories.toString())
        binding.etItemProtein.setText(item.protein.toString())
        binding.etItemFiber.setText(item.fiber.toString())
        binding.etItemFat.setText(item.fat.toString())

        val categories = resources.getStringArray(R.array.item_categories)
        val index = categories.indexOf(item.category)
        if (index >= 0) binding.spinnerCategory.setSelection(index)

        binding.etItemName.requestFocus()
    }

    private fun clearForm() {
        binding.etItemName.text?.clear()
        binding.etItemPrice.text?.clear()
        binding.etItemCalories.text?.clear()
        binding.etItemProtein.text?.clear()
        binding.etItemFiber.text?.clear()
        binding.etItemFat.text?.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
