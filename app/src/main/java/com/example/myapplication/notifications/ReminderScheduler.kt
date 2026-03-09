package com.example.myapplication.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    private const val WORK_NAME_REMINDERS = "senior_health_reminders"

    fun scheduleDailyReminders(context: Context) {
        val workRequest =
            PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS).build()

        WorkManager.getInstance(context.applicationContext).enqueueUniquePeriodicWork(
            WORK_NAME_REMINDERS,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    fun cancelReminders(context: Context) {
        WorkManager.getInstance(context.applicationContext)
            .cancelUniqueWork(WORK_NAME_REMINDERS)
    }
}

