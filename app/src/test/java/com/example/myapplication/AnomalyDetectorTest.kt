package com.example.myapplication

import com.example.myapplication.domain.anomaly.AnomalyDetector
import com.example.myapplication.domain.anomaly.AnomalyLevel
import com.example.myapplication.domain.model.Measurement
import com.example.myapplication.domain.model.MeasurementType
import com.example.myapplication.domain.model.UserSettings
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class AnomalyDetectorTest {

    private val defaultSettings = UserSettings()

    @Test
    fun highTemperatureProducesAlarm() {
        val measurement = Measurement(
            id = 0L,
            timestamp = LocalDateTime.now(),
            type = MeasurementType.TEMPERATURE,
            temperatureCelsius = defaultSettings.temperatureMax + 2.0
        )

        val result = AnomalyDetector.evaluate(measurement, defaultSettings)

        assertEquals(AnomalyLevel.ALARM, result.level)
    }

    @Test
    fun normalBloodPressureIsOk() {
        val measurement = Measurement(
            id = 0L,
            timestamp = LocalDateTime.now(),
            type = MeasurementType.BLOOD_PRESSURE,
            systolicPressure = 120,
            diastolicPressure = 80
        )

        val result = AnomalyDetector.evaluate(measurement, defaultSettings)

        assertEquals(AnomalyLevel.OK, result.level)
    }
}

