package com.example.bitewise.data

import android.content.Context
import com.example.bitewise.ui.screens.FoodItem
import org.json.JSONArray

object FoodCatalog {
    fun getFoodsFromJSON(context: Context): List<FoodItem> {
        val foodList = mutableListOf<FoodItem>()
        try {
            // 1. Open the JSON file from the assets folder
            val inputStream = context.assets.open("food_database.json")
            val jsonString = inputStream.bufferedReader().use { it.readText() }

            // 2. Parse the JSON text into a usable Array
            val jsonArray = JSONArray(jsonString)

            // 3. Loop through all 200 items and convert them into FoodItem objects
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                foodList.add(
                    FoodItem(
                        name = jsonObject.getString("name"),
                        portion = jsonObject.getString("portion"),
                        calories = jsonObject.getInt("calories"),
                        category = jsonObject.getString("category"),
                        initialFavorite = false
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace() // Safely ignore errors so the app never crashes
        }
        return foodList
    }
}

