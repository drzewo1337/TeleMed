package com.example.myapplication.domain.anomaly

import com.example.myapplication.domain.model.Measurement
import com.example.myapplication.domain.model.MeasurementType
import com.example.myapplication.domain.model.UserSettings

enum class AnomalyLevel {
    OK,
    WARNING,
    ALARM
}

data class AnomalyResult(
    val level: AnomalyLevel,
    val message: String?
)

object AnomalyDetector {

    fun evaluate(measurement: Measurement, settings: UserSettings): AnomalyResult {
        return when (measurement.type) {
            MeasurementType.TEMPERATURE -> evaluateTemperature(measurement.temperatureCelsius, settings)
            MeasurementType.BLOOD_PRESSURE -> evaluateBloodPressure(
                systolic = measurement.systolicPressure,
                diastolic = measurement.diastolicPressure,
                settings = settings
            )
            MeasurementType.BLOOD_SUGAR -> evaluateBloodSugar(measurement.bloodSugarMgPerDl, settings)
            MeasurementType.WEIGHT -> evaluateWeight(measurement.weightKg, settings)
        }
    }

    private fun evaluateTemperature(value: Double?, settings: UserSettings): AnomalyResult {
        if (value == null) {
            return AnomalyResult(AnomalyLevel.OK, null)
        }
        if (value >= settings.temperatureMax + 1.0) {
            return AnomalyResult(AnomalyLevel.ALARM, "Wysoka gorączka: $value °C.")
        }
        if (value > settings.temperatureMax) {
            return AnomalyResult(AnomalyLevel.WARNING, "Podwyższona temperatura: $value °C.")
        }
        if (value < settings.temperatureMin) {
            return AnomalyResult(AnomalyLevel.WARNING, "Obniżona temperatura: $value °C.")
        }
        return AnomalyResult(AnomalyLevel.OK, null)
    }

    private fun evaluateBloodPressure(
        systolic: Int?,
        diastolic: Int?,
        settings: UserSettings
    ): AnomalyResult {
        if (systolic == null || diastolic == null) {
            return AnomalyResult(AnomalyLevel.OK, null)
        }
        if (systolic >= settings.systolicMax + 20 || diastolic >= settings.diastolicMax + 10) {
            return AnomalyResult(
                AnomalyLevel.ALARM,
                "Bardzo wysokie ciśnienie: $systolic / $diastolic mmHg."
            )
        }
        if (systolic > settings.systolicMax || diastolic > settings.diastolicMax) {
            return AnomalyResult(
                AnomalyLevel.WARNING,
                "Podwyższone ciśnienie: $systolic / $diastolic mmHg."
            )
        }
        if (systolic < settings.systolicMin || diastolic < settings.diastolicMin) {
            return AnomalyResult(
                AnomalyLevel.WARNING,
                "Obniżone ciśnienie: $systolic / $diastolic mmHg."
            )
        }
        return AnomalyResult(AnomalyLevel.OK, null)
    }

    private fun evaluateBloodSugar(
        value: Double?,
        settings: UserSettings
    ): AnomalyResult {
        if (value == null) {
            return AnomalyResult(AnomalyLevel.OK, null)
        }
        if (value >= settings.bloodSugarMax + 60.0) {
            return AnomalyResult(
                AnomalyLevel.ALARM,
                "Bardzo wysoki poziom cukru: $value mg/dL."
            )
        }
        if (value > settings.bloodSugarMax) {
            return AnomalyResult(
                AnomalyLevel.WARNING,
                "Podwyższony poziom cukru: $value mg/dL."
            )
        }
        if (value < settings.bloodSugarMin) {
            return AnomalyResult(
                AnomalyLevel.WARNING,
                "Obniżony poziom cukru: $value mg/dL."
            )
        }
        return AnomalyResult(AnomalyLevel.OK, null)
    }

    private fun evaluateWeight(value: Double?, settings: UserSettings): AnomalyResult {
        if (value == null) {
            return AnomalyResult(AnomalyLevel.OK, null)
        }
        if (value > settings.weightMax) {
            return AnomalyResult(AnomalyLevel.WARNING, "Masa ciała powyżej zakresu: $value kg.")
        }
        if (value < settings.weightMin) {
            return AnomalyResult(AnomalyLevel.WARNING, "Masa ciała poniżej zakresu: $value kg.")
        }
        return AnomalyResult(AnomalyLevel.OK, null)
    }
}

