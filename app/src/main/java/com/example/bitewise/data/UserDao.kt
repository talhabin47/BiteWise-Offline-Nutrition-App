package com.example.bitewise.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    // Inserts or updates the user profile
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    // Gets the current user profile.
    @Query("SELECT * FROM users LIMIT 1")
    fun getUser(): Flow<UserEntity?>

    // Finds a specific user based on their Firebase UID
    @Query("SELECT * FROM users WHERE userId = :uid LIMIT 1")
    suspend fun getUserById(uid: String): UserEntity?

    // THE OFFLINE-FIRST QUERIES:
    // Finds any profile updates that haven't been backed up to Firebase yet
    @Query("SELECT * FROM users WHERE isSynced = 0")
    suspend fun getUnsyncedUsers(): List<UserEntity>

    // Flips the flag to true once Firebase successfully receives the data
    @Query("UPDATE users SET isSynced = 1 WHERE userId = :id")
    suspend fun markAsSynced(id: String)

    // NEW: The Master Wipe Command! We will trigger this on Logout.
    @Query("DELETE FROM users")
    suspend fun clearAllUsers()
}