package com.example.myapplication.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        MeasurementEntity::class,
        UserSettingsEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun measurementDao(): MeasurementDao

    abstract fun userSettingsDao(): UserSettingsDao
}

