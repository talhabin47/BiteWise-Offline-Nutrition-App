package com.example.bitewise.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val userId: String, // We will use the Firebase UID for this later.
    val name: String,   // NEW: The user's full name!
    val email: String,
    val age: Int,
    val gender: String,
    val height: Double,
    val weight: Double,
    val goal: String,
    val activityLevel: String,
    val dailyCalorieGoal: Int,

    // The Offline-First Magic Flag:
    val isSynced: Boolean = false
)