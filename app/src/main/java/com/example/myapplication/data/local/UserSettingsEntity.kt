package com.example.myapplication.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val temperatureMin: Double,
    val temperatureMax: Double,
    val systolicMin: Int,
    val systolicMax: Int,
    val diastolicMin: Int,
    val diastolicMax: Int,
    val bloodSugarMin: Double,
    val bloodSugarMax: Double,
    val weightMin: Double,
    val weightMax: Double,
    val remindersEnabled: Boolean,
    val morningReminderTime: String?,
    val eveningReminderTime: String?,
    val reminderDaysMask: Int
)

