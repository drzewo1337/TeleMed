package com.example.myapplication.domain.model

import java.time.LocalDateTime

data class Measurement(
    val id: Long = 0L,
    val timestamp: LocalDateTime,
    val type: MeasurementType,
    val temperatureCelsius: Double? = null,
    val systolicPressure: Int? = null,
    val diastolicPressure: Int? = null,
    val bloodSugarMgPerDl: Double? = null,
    val weightKg: Double? = null,
    val note: String? = null
)

