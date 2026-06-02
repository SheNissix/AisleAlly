package com.aisleally.app.data

import com.aisleally.app.model.GroceryItem
import com.aisleally.app.model.OptimizationConstraints
import com.aisleally.app.model.SolutionItem

object Optimizer {

    sealed class OptimizationResult {
        data class Success(val solution: List<SolutionItem>, val score: Double) : OptimizationResult()
        data class Failure(val message: String) : OptimizationResult()
    }

    // Expanded item used in Branch & Bound
    private data class ExpandedItem(
        val id: String,
        val name: String,
        val category: String,
        val price: Double,
        val calories: Double,
        val protein: Double,
        val fiber: Double,
        val fat: Double,
        val nutritionScore: Double,
        val quantityGroup: Int,
        val groupedPrice: Double,
        val groupedCalories: Double,
        val groupedProtein: Double,
        val groupedFiber: Double,
        val groupedFat: Double
    )

    fun run(
        items: List<GroceryItem>,
        constraints: OptimizationConstraints
    ): OptimizationResult {

        val categoryLimits = constraints.categoryLimits
        val budget = constraints.budget
        val minCalories = constraints.minCalories
        val maxCalories = constraints.maxCalories
        val proteinTarget = constraints.proteinTarget
        val fiberTarget = constraints.fiberTarget
        val fatTarget = constraints.fatTarget

        // ── Validate forced quantities ──────────────────────────────────
        for (item in items) {
            if (item.forcedQuantity > item.maxQuantity) {
                return OptimizationResult.Failure(
                    "${item.name}: Forced quantity cannot exceed max quantity."
                )
            }
        }

        // ── Build nutrition scores ──────────────────────────────────────
        val scoredItems = items.map { item ->
            val proteinScore = if (proteinTarget > 0) item.protein / proteinTarget else 0.0
            val fiberScore   = if (fiberTarget   > 0) item.fiber   / fiberTarget   else 0.0
            val fatPenalty   = if (fatTarget     > 0) item.fat     / fatTarget     else 0.0
            item.copy(nutritionScore = proteinScore + fiberScore - fatPenalty)
        }

        // ── Sort by score/price efficiency ─────────────────────────────
        val sortedItems = scoredItems.sortedByDescending {
            if (it.price > 0) it.nutritionScore / it.price else 0.0
        }

        // ── Feasibility checks ─────────────────────────────────────────
        val totalPossibleCalories = sortedItems.sumOf { it.calories * it.maxQuantity }
        if (totalPossibleCalories < minCalories) {
            return OptimizationResult.Failure(
                "No possible combination can reach minimum calories."
            )
        }
        val cheapestPrice = sortedItems.minOfOrNull { it.price } ?: Double.MAX_VALUE
        if (budget < cheapestPrice) {
            return OptimizationResult.Failure("Budget too low for any possible solution.")
        }

        // ── Binary Bounded Decomposition ───────────────────────────────
        val expandedItems = mutableListOf<ExpandedItem>()
        for (item in sortedItems) {
            var remaining = item.maxQuantity - (item.forcedQuantity)
            var power = 1
            while (remaining > 0) {
                val groupQty = minOf(power, remaining)
                expandedItems.add(
                    ExpandedItem(
                        id              = item.id,
                        name            = item.name,
                        category        = item.category,
                        price           = item.price,
                        calories        = item.calories,
                        protein         = item.protein,
                        fiber           = item.fiber,
                        fat             = item.fat,
                        nutritionScore  = item.nutritionScore,
                        quantityGroup   = groupQty,
                        groupedPrice    = item.price    * groupQty,
                        groupedCalories = item.calories * groupQty,
                        groupedProtein  = item.protein  * groupQty,
                        groupedFiber    = item.fiber    * groupQty,
                        groupedFat      = item.fat      * groupQty
                    )
                )
                remaining -= groupQty
                power *= 2
            }
        }

        // ── Pre-load forced items ──────────────────────────────────────
        val startingItems   = mutableListOf<SolutionItem>()
        var startingPrice    = 0.0
        var startingCalories = 0.0
        var startingProtein  = 0.0
        var startingFiber    = 0.0
        var startingFat      = 0.0
        var startProtein     = 0
        var startVegetable   = 0
        var startCarb        = 0
        var startFat         = 0

        for (item in sortedItems) {
            val qty = item.forcedQuantity
            if (qty <= 0) continue
            startingItems.add(
                SolutionItem(item.id, item.name, item.category,
                    item.price, item.calories, item.protein, item.fiber, item.fat, qty)
            )
            startingPrice    += item.price    * qty
            startingCalories += item.calories * qty
            startingProtein  += item.protein  * qty
            startingFiber    += item.fiber    * qty
            startingFat      += item.fat      * qty
            when (item.category) {
                "protein"        -> startProtein   += qty
                "vegetables"     -> startVegetable += qty
                "carbohydrates"  -> startCarb      += qty
                "fats"           -> startFat       += qty
            }
        }

        if (startingPrice > budget) {
            return OptimizationResult.Failure("Forced items already exceed budget.")
        }
        if (startingCalories > maxCalories) {
            return OptimizationResult.Failure("Forced items already exceed maximum calories.")
        }

        // ── Branch and Bound ──────────────────────────────────────────
        var bestScore    = Double.NEGATIVE_INFINITY
        var bestSolution = listOf<SolutionItem>()

        fun branch(
            index          : Int,
            currentItems   : List<SolutionItem>,
            currentPrice   : Double,
            currentCalories: Double,
            currentProtein : Double,
            currentFiber   : Double,
            currentFat     : Double,
            proteinCount   : Int,
            vegetableCount : Int,
            carbCount      : Int,
            fatCount       : Int
        ) {
            // ── Pruning ────────────────────────────────────────────────
            if (currentPrice    > budget)      return
            if (currentCalories > maxCalories) return
            if (proteinCount   > (categoryLimits["protein"]?.max      ?: Int.MAX_VALUE)) return
            if (vegetableCount > (categoryLimits["vegetables"]?.max   ?: Int.MAX_VALUE)) return
            if (carbCount      > (categoryLimits["carbohydrates"]?.max?: Int.MAX_VALUE)) return
            if (fatCount       > (categoryLimits["fats"]?.max         ?: Int.MAX_VALUE)) return

            // ── Upper bound ────────────────────────────────────────────
            val currentScore = (if (proteinTarget > 0) currentProtein / proteinTarget else 0.0) +
                               (if (fiberTarget   > 0) currentFiber   / fiberTarget   else 0.0) -
                               (if (fatTarget     > 0) currentFat     / fatTarget     else 0.0)

            var upperBound       = currentScore
            var remainingBudget  = budget - currentPrice

            for (i in index until expandedItems.size) {
                val ei = expandedItems[i]
                val itemScore = (if (proteinTarget > 0) ei.groupedProtein / proteinTarget else 0.0) +
                                (if (fiberTarget   > 0) ei.groupedFiber   / fiberTarget   else 0.0) -
                                (if (fatTarget     > 0) ei.groupedFat     / fatTarget     else 0.0)
                if (remainingBudget >= ei.groupedPrice) {
                    upperBound      += itemScore
                    remainingBudget -= ei.groupedPrice
                } else {
                    upperBound += itemScore * (remainingBudget / ei.groupedPrice)
                    remainingBudget = 0.0
                    break
                }
                if (remainingBudget <= 0) break
            }

            if (upperBound <= bestScore) return

            // ── Leaf node ──────────────────────────────────────────────
            if (index >= expandedItems.size) {
                if (currentCalories < minCalories) return
                if (proteinCount   < (categoryLimits["protein"]?.min      ?: 0)) return
                if (vegetableCount < (categoryLimits["vegetables"]?.min   ?: 0)) return
                if (carbCount      < (categoryLimits["carbohydrates"]?.min?: 0)) return
                if (fatCount       < (categoryLimits["fats"]?.min         ?: 0)) return

                val totalScore = (if (proteinTarget > 0) currentProtein / proteinTarget else 0.0) +
                                 (if (fiberTarget   > 0) currentFiber   / fiberTarget   else 0.0) -
                                 (if (fatTarget     > 0) currentFat     / fatTarget     else 0.0)
                if (totalScore > bestScore) {
                    bestScore    = totalScore
                    bestSolution = currentItems.map { it.copy() }
                }
                return
            }

            val ei = expandedItems[index]

            // ── Include branch ─────────────────────────────────────────
            val updatedItems = currentItems.map { it.copy() }.toMutableList()
            val existing = updatedItems.find { it.id == ei.id }
            if (existing != null) {
                existing.quantity += ei.quantityGroup
            } else {
                updatedItems.add(
                    SolutionItem(ei.id, ei.name, ei.category,
                        ei.price, ei.calories, ei.protein, ei.fiber, ei.fat, ei.quantityGroup)
                )
            }

            val nextProtein   = proteinCount   + if (ei.category == "protein")        ei.quantityGroup else 0
            val nextVegetable = vegetableCount + if (ei.category == "vegetables")     ei.quantityGroup else 0
            val nextCarb      = carbCount      + if (ei.category == "carbohydrates")  ei.quantityGroup else 0
            val nextFat       = fatCount       + if (ei.category == "fats")           ei.quantityGroup else 0

            branch(index + 1, updatedItems,
                currentPrice    + ei.groupedPrice,
                currentCalories + ei.groupedCalories,
                currentProtein  + ei.groupedProtein,
                currentFiber    + ei.groupedFiber,
                currentFat      + ei.groupedFat,
                nextProtein, nextVegetable, nextCarb, nextFat)

            // ── Exclude branch ─────────────────────────────────────────
            branch(index + 1, currentItems,
                currentPrice, currentCalories, currentProtein, currentFiber, currentFat,
                proteinCount, vegetableCount, carbCount, fatCount)
        }

        branch(0, startingItems,
            startingPrice, startingCalories, startingProtein, startingFiber, startingFat,
            startProtein, startVegetable, startCarb, startFat)

        return if (bestSolution.isEmpty()) {
            OptimizationResult.Failure("No valid solution found with the given constraints.")
        } else {
            OptimizationResult.Success(bestSolution, bestScore)
        }
    }
}
