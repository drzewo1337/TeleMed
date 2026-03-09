package com.example.myapplication.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "measurements",
    indices = [
        Index(value = ["timestamp"]),
        Index(value = ["type"])
    ]
)
data class MeasurementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val timestamp: Long,
    val type: String,
    val temperatureCelsius: Double? = null,
    val systolicPressure: Int? = null,
    val diastolicPressure: Int? = null,
    val bloodSugarMgPerDl: Double? = null,
    val weightKg: Double? = null,
    val note: String? = null
)

