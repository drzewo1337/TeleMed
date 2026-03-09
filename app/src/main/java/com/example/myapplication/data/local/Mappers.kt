package com.example.myapplication.data.local

import com.example.myapplication.domain.model.Measurement
import com.example.myapplication.domain.model.MeasurementType
import com.example.myapplication.domain.model.UserSettings
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

private val zoneId: ZoneId = ZoneId.systemDefault()

private fun LocalDateTime.toEpochMillis(): Long {
    return atZone(zoneId).toInstant().toEpochMilli()
}

private fun Long.toLocalDateTime(): LocalDateTime {
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(this), zoneId)
}

fun MeasurementEntity.toDomain(): Measurement {
    return Measurement(
        id = id,
        timestamp = timestamp.toLocalDateTime(),
        type = runCatching { MeasurementType.valueOf(type) }.getOrDefault(MeasurementType.TEMPERATURE),
        temperatureCelsius = temperatureCelsius,
        systolicPressure = systolicPressure,
        diastolicPressure = diastolicPressure,
        bloodSugarMgPerDl = bloodSugarMgPerDl,
        weightKg = weightKg,
        note = note
    )
}

fun Measurement.toEntity(): MeasurementEntity {
    return MeasurementEntity(
        id = id,
        timestamp = timestamp.toEpochMillis(),
        type = type.name,
        temperatureCelsius = temperatureCelsius,
        systolicPressure = systolicPressure,
        diastolicPressure = diastolicPressure,
        bloodSugarMgPerDl = bloodSugarMgPerDl,
        weightKg = weightKg,
        note = note
    )
}

fun UserSettingsEntity.toDomain(): UserSettings {
    return UserSettings(
        id = id,
        temperatureMin = temperatureMin,
        temperatureMax = temperatureMax,
        systolicMin = systolicMin,
        systolicMax = systolicMax,
        diastolicMin = diastolicMin,
        diastolicMax = diastolicMax,
        bloodSugarMin = bloodSugarMin,
        bloodSugarMax = bloodSugarMax,
        weightMin = weightMin,
        weightMax = weightMax,
        remindersEnabled = remindersEnabled,
        morningReminderTime = morningReminderTime?.let { LocalTime.parse(it) },
        eveningReminderTime = eveningReminderTime?.let { LocalTime.parse(it) },
        reminderDaysMask = reminderDaysMask
    )
}

fun UserSettings.toEntity(): UserSettingsEntity {
    return UserSettingsEntity(
        id = id,
        temperatureMin = temperatureMin,
        temperatureMax = temperatureMax,
        systolicMin = systolicMin,
        systolicMax = systolicMax,
        diastolicMin = diastolicMin,
        diastolicMax = diastolicMax,
        bloodSugarMin = bloodSugarMin,
        bloodSugarMax = bloodSugarMax,
        weightMin = weightMin,
        weightMax = weightMax,
        remindersEnabled = remindersEnabled,
        morningReminderTime = morningReminderTime?.toString(),
        eveningReminderTime = eveningReminderTime?.toString(),
        reminderDaysMask = reminderDaysMask
    )
}

fun LocalDate.startOfDayMillis(): Long {
    return atStartOfDay(zoneId).toInstant().toEpochMilli()
}

fun LocalDate.endOfDayMillis(): Long {
    return atTime(LocalTime.MAX).atZone(zoneId).toInstant().toEpochMilli()
}

