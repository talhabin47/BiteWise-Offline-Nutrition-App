package com.example.bitewise.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// NEW: We bumped the version to 2 because we added the isDeleted flag!
@Database(entities = [UserEntity::class, FoodLogEntity::class], version = 2, exportSchema = false)
abstract class BiteWiseDatabase : RoomDatabase() {

    // Connect the DAOs (instruction manuals) to the database
    abstract fun userDao(): UserDao
    abstract fun foodLogDao(): FoodLogDao

    companion object {
        @Volatile
        private var INSTANCE: BiteWiseDatabase? = null

        // This ensures only ONE copy of the database is ever open at the same time
        fun getDatabase(context: Context): BiteWiseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BiteWiseDatabase::class.java,
                    "bitewise_offline_database"
                )
                    .fallbackToDestructiveMigration() // If we change tables later, it safely rebuilds
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}