package com.example.myapplication.domain.model

import java.time.LocalTime

data class UserSettings(
    val id: Int = 1,
    val temperatureMin: Double = 36.0,
    val temperatureMax: Double = 37.5,
    val systolicMin: Int = 90,
    val systolicMax: Int = 140,
    val diastolicMin: Int = 60,
    val diastolicMax: Int = 90,
    val bloodSugarMin: Double = 70.0,
    val bloodSugarMax: Double = 140.0,
    val weightMin: Double = 40.0,
    val weightMax: Double = 150.0,
    val remindersEnabled: Boolean = true,
    val morningReminderTime: LocalTime? = LocalTime.of(8, 0),
    val eveningReminderTime: LocalTime? = LocalTime.of(20, 0),
    /**
     * Bitmask dla dni tygodnia (1 = pon, 2 = wt, 4 = śr, 8 = czw, 16 = pt, 32 = sob, 64 = niedz).
     */
    val reminderDaysMask: Int = 0b0111_1111
)

