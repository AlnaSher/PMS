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
import com.example.myapplication.database.DatabaseHelper
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
    fun scheduleOvulationNotification(context: Context, userId: Long, duration: Int) {
        val dbHelper = DatabaseHelper(context)

        // Получаем последнюю дату овуляции из базы данных
        val lastOvulationDate = dbHelper.getLastOvulationDate(userId) // Этот метод должен вернуть LocalDate?
        val today = LocalDate.now()
        val nextOvulationDate = if (lastOvulationDate == null || lastOvulationDate.isBefore(today)) {
            lastOvulationDate?.plusDays(duration.toLong())
        } else {
            lastOvulationDate
        } ?: return  // Выход, если дата не определена

        // Устанавливаем дату уведомления за день до овуляции в 12:00
        val notificationDateTime = nextOvulationDate.minusDays(1).atTime(12, 0)
        val notificationMillis = notificationDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        // Настраиваем Alarm для уведомления
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, PeriodNotificationReceiver::class.java).apply {
            putExtra("notification_type", "ovulation")
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            userId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Запланировать Alarm на указанное время
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, notificationMillis, pendingIntent)
    }
}