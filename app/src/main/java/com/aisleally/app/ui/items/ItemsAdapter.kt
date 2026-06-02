package com.aisleally.app.ui.items

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.aisleally.app.databinding.ItemRowBinding
import com.aisleally.app.model.GroceryItem

class ItemsAdapter(
    private val onEditClick       : (GroceryItem) -> Unit,
    private val onMaxQtyChanged   : (String, Int) -> Unit,
    private val onForcedQtyChanged: (String, Int) -> Unit
) : ListAdapter<GroceryItem, ItemsAdapter.ItemViewHolder>(DiffCallback) {

    inner class ItemViewHolder(private val binding: ItemRowBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: GroceryItem) {
            binding.tvItemName.text = item.name
            binding.tvItemMeta.text =
                "₱${item.price.toInt()} | ${item.calories.toInt()} kcal | " +
                "${item.protein.toInt()}g protein | ${item.fiber.toInt()}g fiber | ${item.fat.toInt()}g fat"

            // Prevent TextWatcher cascade
            binding.etMaxQty.removeTextChangedListener(binding.etMaxQty.tag as? TextWatcher)
            binding.etForcedQty.removeTextChangedListener(binding.etForcedQty.tag as? TextWatcher)

            binding.etMaxQty.setText(item.maxQuantity.toString())
            binding.etForcedQty.setText((item.forcedQuantity).toString())

            val maxWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val v = s.toString().toIntOrNull() ?: return
                    onMaxQtyChanged(item.id, v)
                }
            }
            val forcedWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val v = s.toString().toIntOrNull() ?: return
                    onForcedQtyChanged(item.id, v)
                }
            }

            binding.etMaxQty.tag = maxWatcher
            binding.etForcedQty.tag = forcedWatcher
            binding.etMaxQty.addTextChangedListener(maxWatcher)
            binding.etForcedQty.addTextChangedListener(forcedWatcher)

            binding.btnEdit.setOnClickListener { onEditClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ItemRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<GroceryItem>() {
        override fun areItemsTheSame(oldItem: GroceryItem, newItem: GroceryItem) =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: GroceryItem, newItem: GroceryItem) =
            oldItem == newItem
    }
}
