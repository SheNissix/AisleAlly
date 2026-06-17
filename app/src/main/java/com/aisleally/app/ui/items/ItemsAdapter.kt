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
    private val onForcedQtyChanged: (String, Int) -> Unit,
    private val onDeleteClick     : (String) -> Unit
) : ListAdapter<GroceryItem, ItemsAdapter.ItemViewHolder>(DiffCallback) {

    inner class ItemViewHolder(private val binding: ItemRowBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: GroceryItem) {
            binding.tvItemName.text = item.name
            binding.tvItemMeta.text =
                "₱${String.format(java.util.Locale.US, "%.2f", item.price)} | ${Math.round(item.calories)} kcal | " +
                "${Math.round(item.protein)}g protein | ${Math.round(item.fiber)}g fiber | ${Math.round(item.fat)}g fat"

            // Prevent TextWatcher cascade
            binding.etMaxQty.removeTextChangedListener(binding.etMaxQty.tag as? TextWatcher)
            binding.etForcedQty.removeTextChangedListener(binding.etForcedQty.tag as? TextWatcher)

            binding.etMaxQty.setText(item.maxQuantity.toString())
            binding.etForcedQty.setText((item.forcedQuantity).toString())

            val maxWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val v = s.toString().toIntOrNull() ?: 1
                    onMaxQtyChanged(item.id, v)
                }
            }
            val forcedWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val v = s.toString().toIntOrNull() ?: 0
                    onForcedQtyChanged(item.id, v)
                }
            }

            binding.etMaxQty.tag = maxWatcher
            binding.etForcedQty.tag = forcedWatcher
            binding.etMaxQty.addTextChangedListener(maxWatcher)
            binding.etForcedQty.addTextChangedListener(forcedWatcher)

            binding.btnEdit.setOnClickListener { onEditClick(item) }
            binding.btnDelete.setOnClickListener { onDeleteClick(item.id) }
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
