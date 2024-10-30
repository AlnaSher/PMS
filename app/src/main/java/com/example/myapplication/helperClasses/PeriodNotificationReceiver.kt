package com.example.myapplication.helperClasses

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.myapplication.R
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar

class PeriodNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationType = intent.getStringExtra("notification_type")
        sendNotification(context, notificationType)
    }

    @SuppressLint("MissingPermission")
    private fun sendNotification(context: Context, notificationType: String?) {
        val title: String
        val content: String

        when (notificationType) {
            "start_cycle" -> {
                title = "Напоминание: Начало цикла"
                content = "Завтра начинается новый цикл."
            }
            "ovulation" -> {
                title = "Напоминание: Овуляция"
                content = "Сегодня овуляция."
            }
            "period_reminder" -> {
                title = "Напоминание: Начало месячных"
                content = "Начало месячных через пару дней."
            }
            else -> {
                title = "Напоминание"
                content = "У вас есть напоминание."
            }
        }

        val notificationBuilder = NotificationCompat.Builder(context, "YOUR_CHANNEL_ID")
            .setSmallIcon(R.drawable.free_icon_notification_2326010)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(1, notificationBuilder.build())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun schedulePeriodNotification(context: Context, userId: Long, nextCycleDate: LocalDate, notificationType: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Устанавливаем дату и время для уведомления (на основе типа уведомления)
        val calendar = Calendar.getInstance().apply {
            timeInMillis = when (notificationType) {
                "start_cycle" -> nextCycleDate.minusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                "ovulation" -> nextCycleDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                "period_reminder" -> nextCycleDate.minusDays(2).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() // Например, за два дня
                else -> return // Неизвестный тип уведомления
            }
        }

        val intent = Intent(context, PeriodNotificationReceiver::class.java).apply {
            putExtra("notification_type", notificationType)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            userId.toInt(), // или уникальный ID для пользователя
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Устанавливаем alarm, который сработает в указанное время
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
    }
}
