package com.example.myapplication.domain.anomaly

import android.content.Context
import com.example.myapplication.data.repository.SettingsRepository
import com.example.myapplication.domain.model.Measurement
import com.example.myapplication.notifications.NotificationHelper

class MeasurementAlertService(
    private val appContext: Context,
    private val settingsRepository: SettingsRepository
) {

    suspend fun handleNewMeasurement(measurement: Measurement) {
        val settings = settingsRepository.getUserSettings()
        val result = AnomalyDetector.evaluate(measurement, settings)
        if (result.level == AnomalyLevel.ALARM) {
            NotificationHelper.ensureChannels(appContext)
            NotificationHelper.showAnomalyNotification(appContext, measurement, result)
        }
    }
}

