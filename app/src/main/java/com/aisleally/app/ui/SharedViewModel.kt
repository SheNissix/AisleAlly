package com.aisleally.app.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aisleally.app.data.ItemRepository
import com.aisleally.app.data.Optimizer
import com.aisleally.app.model.GroceryItem
import com.aisleally.app.model.NutritionTargets
import com.aisleally.app.model.OptimizationConstraints
import com.aisleally.app.model.SolutionItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SharedViewModel : ViewModel() {

    // ── Items ────────────────────────────────────────────────────────
    val items: MutableLiveData<List<GroceryItem>> = MutableLiveData(ItemRepository.items.toList())

    fun refreshItems() {
        items.value = ItemRepository.items.toList()
    }

    fun addOrUpdateItem(item: GroceryItem) {
        if (ItemRepository.findById(item.id) != null) {
            ItemRepository.update(item)
        } else {
            ItemRepository.add(item)
        }
        refreshItems()
    }

    fun updateMaxQuantity(id: String, value: Int) {
        ItemRepository.findById(id)?.let { it.maxQuantity = value }
        refreshItems()
    }

    fun updateForcedQuantity(id: String, value: Int): String? {
        val item = ItemRepository.findById(id) ?: return null
        if (value > item.maxQuantity) {
            return "Forced quantity cannot exceed max quantity."
        }
        item.forcedQuantity = value
        refreshItems()
        return null
    }

    // ── Nutrition targets ────────────────────────────────────────────
    val nutritionTargets: MutableLiveData<NutritionTargets> = MutableLiveData(NutritionTargets())

    fun generateTargets(age: Int, weight: Double, height: Double, sex: String, activity: String) {
        val multiplier = mapOf(
            "Sedentary"   to 1.2,
            "Light"       to 1.375,
            "Moderate"    to 1.55,
            "Very Active" to 1.725
        )[activity] ?: 1.2

        val bmr = if (sex == "Male") {
            10 * weight + 6.25 * height - 5 * age + 5
        } else {
            10 * weight + 6.25 * height - 5 * age - 161
        }
        val tdee = (bmr * multiplier).toInt()

        nutritionTargets.value = NutritionTargets(
            dailyCalories = tdee,
            minCalories   = (tdee * 0.9).toInt(),
            maxCalories   = (tdee * 1.1).toInt(),
            protein       = (1.2 * weight).toInt(),
            fiber         = (14.0 * (tdee / 1000.0)).toInt(),
            fat           = ((0.30 * tdee) / 9.0).toInt()
        )
    }

    // ── Optimization ─────────────────────────────────────────────────
    val constraints: MutableLiveData<OptimizationConstraints> =
        MutableLiveData(OptimizationConstraints())

    val optimizationResult: MutableLiveData<Optimizer.OptimizationResult?> = MutableLiveData(null)

    fun runOptimization(c: OptimizationConstraints) {
        constraints.value = c
        viewModelScope.launch {
            val result = withContext(Dispatchers.Default) {
                Optimizer.run(ItemRepository.items, c)
            }
            optimizationResult.value = result
        }
    }

    fun clearOptimizationResult() {
        optimizationResult.value = null
    }
}
