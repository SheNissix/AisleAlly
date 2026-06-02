package com.aisleally.app.model

import java.io.Serializable

data class GroceryItem(
    val id: String,
    val name: String,
    val category: String = "protein",
    val price: Double,
    val calories: Double,
    val protein: Double,
    val fiber: Double,
    val fat: Double,
    var maxQuantity: Int = 1,
    var forcedQuantity: Int = 0,
    // Used internally during optimization
    var nutritionScore: Double = 0.0
) : Serializable

data class SolutionItem(
    val id: String,
    val name: String,
    val category: String,
    val price: Double,
    val calories: Double,
    val protein: Double,
    val fiber: Double,
    val fat: Double,
    var quantity: Int
)

data class NutritionTargets(
    val dailyCalories: Int = 0,
    val minCalories: Int = 0,
    val maxCalories: Int = 0,
    val protein: Int = 0,
    val fiber: Int = 0,
    val fat: Int = 0
)

data class CategoryLimit(
    val min: Int = 0,
    val max: Int = Int.MAX_VALUE
)

data class OptimizationConstraints(
    var budget: Double = 0.0,
    var days: Int = 0,
    var minCalories: Double = 0.0,
    var maxCalories: Double = 0.0,
    var proteinTarget: Double = 0.0,
    var fiberTarget: Double = 0.0,
    var fatTarget: Double = 0.0,
    var categoryLimits: Map<String, CategoryLimit> = mapOf(
        "protein" to CategoryLimit(),
        "vegetables" to CategoryLimit(),
        "carbohydrates" to CategoryLimit(),
        "fats" to CategoryLimit()
    )
)
