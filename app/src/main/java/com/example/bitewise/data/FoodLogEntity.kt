package com.example.bitewise.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food_logs")
data class FoodLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0, // Room automatically generates this number!

    val name: String,
    val portion: String,
    val calories: Int,
    val category: String, // e.g., "Breakfast 🍳", "Lunch 🥗"
    val isFavorite: Boolean,
    val timestamp: Long, // Saves the exact date and time it was logged

    // The Offline-First Magic Flags:
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false // NEW: The Soft Delete Flag!
)