package com.example.bitewise.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodLogDao {
    // Saves a new food entry to the database
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodLog(foodLog: FoodLogEntity)

    // Deletes a specific food log if the user made a mistake
    @Delete
    suspend fun deleteFoodLog(foodLog: FoodLogEntity)

    // Gets all food logs to display on the screen, newest first
    @Query("SELECT * FROM food_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<FoodLogEntity>>

    // Gets only the foods you clicked the Star on!
    @Query("SELECT * FROM food_logs WHERE isFavorite = 1")
    fun getFavoriteFoods(): Flow<List<FoodLogEntity>>

    // NEW FIX: Removes food from favorites AND flags it for sync!
    @Query("UPDATE food_logs SET isFavorite = 0, isSynced = 0 WHERE name = :foodName")
    suspend fun removeFavorite(foodName: String)

    // THE OFFLINE-FIRST QUERIES:
    // Finds all meals logged while the phone was offline
    @Query("SELECT * FROM food_logs WHERE isSynced = 0")
    suspend fun getUnsyncedLogs(): List<FoodLogEntity>

    // Flips the flag to true for multiple items at once after a Firebase upload
    @Query("UPDATE food_logs SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<Int>)

    // NEW: The Master Wipe Command! We will trigger this on Logout.
    @Query("DELETE FROM food_logs")
    suspend fun clearAllLogs()
}