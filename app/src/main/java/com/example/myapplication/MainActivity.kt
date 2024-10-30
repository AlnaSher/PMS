package com.example.myapplication

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.example.myapplication.activities.LoginActivity
import com.example.myapplication.activities.ProfileActivity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Получение сохраненных данных
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userId = sharedPreferences.getLong("user_id", -1L)
        val rememberMe = sharedPreferences.getBoolean("remember_me", false) // Получаем флаг "Запомнить меня"

        createNotificationChannel(applicationContext)

        if (userId != -1L && rememberMe) {
            // Если пользователь запомнен, переход на профиль
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("user_id", userId) // Передача ID
            startActivity(intent)
            finish()
        } else {
            // Если ID не найден или флаг "Запомнить меня" не установлен, перенаправить на страницу авторизации
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}

fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = "Cycle Reminder Channel"
        val descriptionText = "Channel for period reminder notifications"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel("YOUR_CHANNEL_ID", name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}