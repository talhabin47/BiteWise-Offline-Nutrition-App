package com.example.bitewise.data

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class SyncRepository(
    private val userDao: UserDao,
    private val foodLogDao: FoodLogDao // NEW: We brought in the Food database!
) {

    // Connect to the Firestore database & Auth
    private val firestore = Firebase.firestore
    private val auth = Firebase.auth

    // --- 1. SYNC USER PROFILE ---
    suspend fun syncUsersToCloud() {
        try {
            // Ask Room for ANY users that haven't been backed up yet (isSynced = 0)
            val unsyncedUsers = userDao.getUnsyncedUsers()

            for (user in unsyncedUsers) {

                // Package the Room Entity into a format Firestore understands
                val userData = hashMapOf(
                    "userId" to user.userId,
                    "name" to user.name,
                    "email" to user.email,
                    "age" to user.age,
                    "gender" to user.gender,
                    "height" to user.height,
                    "weight" to user.weight,
                    "goal" to user.goal,
                    "activityLevel" to user.activityLevel,
                    "dailyCalorieGoal" to user.dailyCalorieGoal
                )

                // Upload it to the "users" collection
                firestore.collection("users").document(user.userId)
                    .set(userData)
                    .await()

                // If it succeeds, tell Room to flip the flag to TRUE!
                userDao.markAsSynced(user.userId)
                Log.d("SyncEngine", "Successfully backed up user: ${user.userId}")
            }
        } catch (e: Exception) {
            Log.e("SyncEngine", "Failed to sync users: ${e.message}")
        }
    }

    // --- 2. SYNC FOOD LOGS ---
    suspend fun syncFoodLogsToCloud() {
        val uid = auth.currentUser?.uid ?: return // Stop if nobody is logged in!

        try {
            // Ask Room for all meals logged while offline (includes Soft Deleted items!)
            val unsyncedLogs = foodLogDao.getUnsyncedLogs()

            if (unsyncedLogs.isEmpty()) {
                Log.d("SyncEngine", "No new food logs to sync.")
                return
            }

            val successfullySyncedIds = mutableListOf<Int>()

            for (log in unsyncedLogs) {
                // Find the exact document in Firebase
                val docRef = firestore.collection("users").document(uid)
                    .collection("food_logs").document(log.timestamp.toString())

                if (log.isDeleted) {
                    // NEW: SOFT DELETE LOGIC!
                    // 1. Delete it from Firebase permanently
                    docRef.delete().await()

                    // 2. Now that Firebase knows it's gone, delete it from the phone permanently
                    foodLogDao.deleteFoodLog(log)
                    Log.d("SyncEngine", "Permanently deleted food log: ${log.name}")

                } else {
                    // REGULAR UPLOAD LOGIC
                    val logData = hashMapOf(
                        "name" to log.name,
                        "portion" to log.portion,
                        "calories" to log.calories,
                        "category" to log.category,
                        "isFavorite" to log.isFavorite,
                        "timestamp" to log.timestamp
                    )

                    // Push meal into cloud
                    docRef.set(logData).await()
                    successfullySyncedIds.add(log.id)
                }
            }

            // Flip the flags to true in Room for everything we just uploaded!
            if (successfullySyncedIds.isNotEmpty()) {
                foodLogDao.markAsSynced(successfullySyncedIds)
                Log.d("SyncEngine", "Successfully synced ${successfullySyncedIds.size} food logs!")
            }

        } catch (e: Exception) {
            Log.e("SyncEngine", "Failed to sync food logs: ${e.message}")
        }
    }

    // --- 3. THE MASTER TRIGGER ---
    suspend fun syncAllData() {
        syncUsersToCloud()
        syncFoodLogsToCloud()
    }

    // --- 4. THE CLOUD RESTORE ENGINE (Run this on Login!) ---
    suspend fun restoreDataFromCloud() {
        val uid = auth.currentUser?.uid ?: return

        try {
            // 1. Pull down the User Profile
            val userDoc = firestore.collection("users").document(uid).get().await()
            if (userDoc.exists()) {
                val user = UserEntity(
                    userId = uid,
                    name = userDoc.getString("name") ?: "",
                    email = userDoc.getString("email") ?: "",
                    age = userDoc.getLong("age")?.toInt() ?: 28,
                    gender = userDoc.getString("gender") ?: "Male",
                    height = userDoc.getDouble("height") ?: 0.0,
                    weight = userDoc.getDouble("weight") ?: 0.0,
                    goal = userDoc.getString("goal") ?: "Loss",
                    activityLevel = userDoc.getString("activityLevel") ?: "",
                    dailyCalorieGoal = userDoc.getLong("dailyCalorieGoal")?.toInt() ?: 2000,
                    isSynced = true // It came from the cloud, so it's already synced!
                )
                userDao.insertUser(user)
            } else {
                // --- NEW: THE SAFETY NET ---
                // If Firebase has no data, create a fresh profile for them locally!
                val defaultUser = UserEntity(
                    userId = uid,
                    name = "New User",
                    email = auth.currentUser?.email ?: "",
                    age = 25,
                    gender = "Male",
                    height = 170.0,
                    weight = 70.0,
                    goal = "Maintenance",
                    activityLevel = "Moderate (exercise 3-5 days/week)",
                    dailyCalorieGoal = 2000,
                    isSynced = false // Flags it to sync up to Firebase immediately!
                )
                userDao.insertUser(defaultUser)
            }

            // 2. Pull down all Food Logs
            val logsSnapshot = firestore.collection("users").document(uid).collection("food_logs").get().await()
            for (doc in logsSnapshot.documents) {
                val log = FoodLogEntity(
                    name = doc.getString("name") ?: "",
                    portion = doc.getString("portion") ?: "",
                    calories = doc.getLong("calories")?.toInt() ?: 0,
                    category = doc.getString("category") ?: "",
                    isFavorite = doc.getBoolean("isFavorite") ?: false,
                    timestamp = doc.getLong("timestamp") ?: 0L,
                    isSynced = true,
                    isDeleted = false // It's downloaded from the cloud, so it's definitely not deleted!
                )
                foodLogDao.insertFoodLog(log)
            }
            Log.d("SyncEngine", "Successfully restored all data from Firebase!")

        } catch (e: Exception) {
            Log.e("SyncEngine", "Failed to restore data: ${e.message}")
        }
    }
}