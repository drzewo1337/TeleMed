package com.example.myapplication.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.myapplication.data.local.AppDatabaseProvider
import com.example.myapplication.data.repository.SettingsRepository

class ReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val database = AppDatabaseProvider.getDatabase(applicationContext)
        val settingsRepository = SettingsRepository(database.userSettingsDao())
        val settings = settingsRepository.getUserSettings()

        if (!settings.remindersEnabled) {
            return Result.success()
        }

        NotificationHelper.ensureChannels(applicationContext)
        NotificationHelper.showReminderNotification(
            context = applicationContext,
            message = "Czas na pomiar parametrów zdrowotnych."
        )

        return Result.success()
    }
}

