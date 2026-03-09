package com.example.myapplication.data.local

import android.content.Context
import androidx.room.Room

object AppDatabaseProvider {

    @Volatile
    private var instance: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        val existing = instance
        if (existing != null) {
            return existing
        }
        return synchronized(this) {
            val again = instance
            if (again != null) {
                return@synchronized again
            }
            val created = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "senior_health_diary.db"
            ).build()
            instance = created
            created
        }
    }
}

