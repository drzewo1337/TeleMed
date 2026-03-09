package com.example.myapplication.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.domain.anomaly.AnomalyResult
import com.example.myapplication.domain.model.Measurement
import com.example.myapplication.domain.model.MeasurementType

object NotificationHelper {

    private const val CHANNEL_ID_ALERTS = "senior_health_alerts"
    private const val CHANNEL_NAME_ALERTS = "Alerty zdrowotne"
    private const val CHANNEL_ID_REMINDERS = "senior_health_reminders"
    private const val CHANNEL_NAME_REMINDERS = "Przypomnienia o pomiarach"

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val alertsChannel = NotificationChannel(
            CHANNEL_ID_ALERTS,
            CHANNEL_NAME_ALERTS,
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(alertsChannel)

        val remindersChannel = NotificationChannel(
            CHANNEL_ID_REMINDERS,
            CHANNEL_NAME_REMINDERS,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(remindersChannel)
    }

    fun showAnomalyNotification(
        context: Context,
        measurement: Measurement,
        result: AnomalyResult
    ) {
        val pendingIntent = createMainActivityPendingIntent(context)

        val title = when (measurement.type) {
            MeasurementType.TEMPERATURE -> "Alert: temperatura"
            MeasurementType.BLOOD_PRESSURE -> "Alert: ciśnienie"
            MeasurementType.BLOOD_SUGAR -> "Alert: poziom cukru"
            MeasurementType.WEIGHT -> "Alert: masa ciała"
        }

        val content = result.message ?: "Zarejestrowano nieprawidłowy pomiar."

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_ALERTS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        with(NotificationManagerCompat.from(context)) {
            notify(createNotificationId(), builder.build())
        }
    }

    fun showReminderNotification(
        context: Context,
        message: String
    ) {
        val pendingIntent = createMainActivityPendingIntent(context)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_REMINDERS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Przypomnienie o pomiarze")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        with(NotificationManagerCompat.from(context)) {
            notify(createNotificationId(), builder.build())
        }
    }

    private fun createMainActivityPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createNotificationId(): Int {
        return (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
    }
}

