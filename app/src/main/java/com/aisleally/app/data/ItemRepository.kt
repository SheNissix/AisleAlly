package com.aisleally.app.data

import com.aisleally.app.model.GroceryItem

object ItemRepository {

    val items: MutableList<GroceryItem> = mutableListOf(
        GroceryItem("1", "Chicken Breast", "protein",    180.0, 300.0, 55.0, 0.0,  8.0,  1, 0),
        GroceryItem("2", "Brown Rice",     "carbohydrates", 60.0, 350.0,  7.0, 4.0,  2.0,  1, 0),
        GroceryItem("3", "Eggs",           "protein",     50.0, 150.0, 12.0, 0.0, 10.0,  1, 0),
        GroceryItem("4", "Oatmeal",        "carbohydrates", 90.0, 250.0, 10.0, 8.0,  5.0,  1, 0),
        GroceryItem("5", "Banana",         "carbohydrates", 15.0, 105.0,  1.0, 3.0,  0.0,  1, 0),
        GroceryItem("6", "Milk",           "fats",        85.0, 180.0,  9.0, 0.0, 10.0,  1, 0),
        GroceryItem("7", "Peanut Butter",  "fats",       120.0, 190.0,  8.0, 2.0, 16.0,  1, 0),
        GroceryItem("8", "Broccoli",       "vegetables",  70.0,  55.0,  4.0, 5.0,  1.0,  1, 0),
        GroceryItem("9", "Tilapia",        "protein",    140.0, 220.0, 40.0, 0.0,  6.0,  1, 0),
        GroceryItem("10","Sweet Potato",   "carbohydrates", 45.0, 180.0,  4.0, 6.0,  0.0,  1, 0)
    )

    fun findById(id: String): GroceryItem? = items.find { it.id == id }

    fun add(item: GroceryItem) { items.add(item) }

    fun update(updated: GroceryItem) {
        val index = items.indexOfFirst { it.id == updated.id }
        if (index != -1) items[index] = updated
    }

    fun remove(id: String) { items.removeAll { it.id == id } }
}
