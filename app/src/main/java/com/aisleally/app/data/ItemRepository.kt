package com.aisleally.app.data

import android.content.Context
import com.aisleally.app.model.GroceryItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object ItemRepository {

    private const val PREFS_NAME = "aisleally_prefs"
    private const val KEY_ITEMS = "grocery_items"
    
    private var prefs: android.content.SharedPreferences? = null
    private val gson = Gson()

    val items: MutableList<GroceryItem> = mutableListOf(
        GroceryItem("1", "Chicken Breast 100g", "protein",    25.0, 195.0, 30.0, 0.0,  8.0,  10, 0),
        GroceryItem("2", "Healthy You Brown Rice 1kg",     "carbohydrates", 101.0, 1107.0,  26.0, 21.0,  10.0,  10, 0),
        GroceryItem("3", "Eggs 1 Dozen",           "protein",     130.0, 882.0, 76.0, 0.0, 60.0,  10, 0),
        GroceryItem("4", "Healthy You Rolled Oats 500g",        "carbohydrates", 99.0, 1785.0, 0.0, 55.0,  45.0,  10, 0),
        GroceryItem("5", "Lacatan Banana 250g",         "carbohydrates", 48.0, 222.0,  3.0, 7.0,  1.0,  10, 0),
        GroceryItem("6", "Bear Brand Fortified Milk 28g",           "fats",        11.0, 153.0,  5.0, 0.0, 7.0,  10, 0),
        GroceryItem("7", "Lily's Peanut Butter 504g",  "fats",       211.0, 3120.0,  104.0, 39.0, 208.0,  10, 0),
        GroceryItem("8", "Farmland Mixed Vegetables 400g",       "vegetables",  75.0,  200.0,  12.0, 9.0,  2.0,  10, 0),
        GroceryItem("9", "Century Tuna 180g",        "protein",    50.0, 297.0, 21.0, 0.0,  21.0,  10, 0),
        GroceryItem("10","Dinorado Rice 1kg",   "carbohydrates", 67.0, 3550.0,  70.0, 5.0,  5.0,  10, 0),
        GroceryItem("11", "Young's Town Sardines 155g", "protein", 24.0, 116.0, 16.0, 0.0,4.0, 10, 0),
        GroceryItem("12", "Gardenia High Fiber Wheat 400g", "carbohydrates", 80.0, 966.0, 42.0, 30.0, 6.0, 10, 0),
        GroceryItem("13", "Jolly Mushrooms Stems 400g", "carbohydrates", 59.0, 400.0, 12.0, 16.0, 0.0, 10, 0),
        GroceryItem("14", "McCornick Iodized Salt 140g", "others", 32.00, 0.0, 0.0, 0.0, 0.0, 10, 0),
        GroceryItem("15", "San Marino Corned Tuna 155g", "protein", 46.0, 279.0, 21.0, 12.0, 15.0, 10,0),
        GroceryItem("16", "Purefoods Fun Nuggets 200g", "protein", 104.0, 448.0, 20.0,16.0,20.0,10, 0),
        GroceryItem("17", "Purefoods TenderJuicy Jumbo 500g", "protein", 141.0, 2871.0, 132.0, 33.0, 165.0, 10,0),
        GroceryItem("18", "Argentina Rdy To Use Giniling 150g", "protein", 32.0, 285.0, 21.0,9.0, 18.0, 10, 0),
        GroceryItem("19", "Lady's Choice Mayonnaise 220ml", "fats", 122.0, 1365.0, 0.0, 0.0, 143.0, 10, 0),
        GroceryItem("20", "Pork Adobo 100g", "protein", 40.0, 165.0, 15.0, 0.0, 9.0, 10, 0)

    )

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        loadFromPrefs()
    }

    private fun loadFromPrefs() {
        val json = prefs?.getString(KEY_ITEMS, null)
        if (json != null) {
            val type = object : TypeToken<List<GroceryItem>>() {}.type
            val savedItems: List<GroceryItem> = gson.fromJson(json, type)
            items.clear()
            items.addAll(savedItems)
        }
    }

    private fun saveToPrefs() {
        prefs?.edit()?.putString(KEY_ITEMS, gson.toJson(items))?.apply()
    }

    fun findById(id: String): GroceryItem? = items.find { it.id == id }

    fun add(item: GroceryItem) { 
        items.add(item) 
        saveToPrefs()
    }

    fun update(updated: GroceryItem) {
        val index = items.indexOfFirst { it.id == updated.id }
        if (index != -1) {
            items[index] = updated
            saveToPrefs()
        }
    }

    fun remove(id: String) { 
        items.removeAll { it.id == id } 
        saveToPrefs()
    }
}
