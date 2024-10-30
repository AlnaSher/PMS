package com.example.myapplication.activities

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.myapplication.R
import com.example.myapplication.database.DatabaseHelper
import com.example.myapplication.helperClasses.PeriodNotificationReceiver
import java.time.LocalDate

class CommonSettingsViewActivity : ComponentActivity() {

    private lateinit var nameTextView: TextView
    private lateinit var loginTextView: TextView
    private lateinit var switchStartOfPeriod: Switch
    private lateinit var switchOvulation: Switch
    private lateinit var switchRememberMe: Switch
    private lateinit var switchPeriodInDays: Switch
    private lateinit var buttonChange: Button
    private lateinit var cycleSettingsButton: Button

    private lateinit var databaseHelper: DatabaseHelper
    private val CHANNEL_ID = "settings_notifications_channel"

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val isTablet = resources.getBoolean(R.bool.is_tablet)
        if (!isTablet) {
            // Если это телефон, фиксируем ориентацию в портретной
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        setContentView(R.layout.activity_common_settings_view)

        // Инициализация элементов
        nameTextView = findViewById(R.id.textView2)
        loginTextView = findViewById(R.id.textView3)
        switchStartOfPeriod = findViewById(R.id.switch1)
        switchOvulation = findViewById(R.id.switch2)
        switchRememberMe = findViewById(R.id.switch4)
        switchPeriodInDays = findViewById(R.id.switch3)
        buttonChange = findViewById(R.id.button3)
        cycleSettingsButton = findViewById(R.id.button)

        val imageViewBack = findViewById<ImageView>(R.id.imageViewBack)
        imageViewBack.setImageResource(R.drawable.free_icon_back_arrow_5637688)

        val userId = intent.getLongExtra("user_id", -1)
        databaseHelper = DatabaseHelper(this)

        createNotificationChannel()

        loadSettings(userId)

        // Установка слушателей на переключатели с отправкой уведомлений
        switchStartOfPeriod.setOnCheckedChangeListener { _, isChecked ->
            databaseHelper.updateCycleStartEnabled(userId, isChecked)
            if (isChecked) scheduleNotification("start_cycle", userId)
            else cancelNotification(userId) // Отменяем уведомление, если выключено
            showToast("Настройка 'Начало месячных' сохранена")
        }

        switchOvulation.setOnCheckedChangeListener { _, isChecked ->
            databaseHelper.updateOvulationEnabled(userId, isChecked)
            if (isChecked) scheduleNotification("ovulation", userId)
            else cancelNotification(userId) // Отменяем уведомление, если выключено
            showToast("Настройка 'Овуляция' сохранена")
        }

        switchRememberMe.setOnCheckedChangeListener { _, isChecked ->
            databaseHelper.updateRememberMeEnabled(userId, isChecked)
            saveRememberMePreference(isChecked)
            showToast("Настройка 'Запомнить меня' сохранена")
        }

        switchPeriodInDays.setOnCheckedChangeListener { _, isChecked ->
            databaseHelper.updatePeriodEnabled(userId, isChecked)
            if (isChecked) scheduleNotification("period_reminder", userId)
            else cancelNotification(userId) // Отменяем уведомление, если выключено
            showToast("Настройка 'Месячные через пару дней' сохранена")
        }

        buttonChange.setOnClickListener {
            val intent = Intent(this, CommonSettingsUpdateActivity::class.java)
            intent.putExtra("user_id", userId)
            startActivity(intent)
            finish()
        }

        cycleSettingsButton.setOnClickListener {
            val intent = Intent(this, CycleSettingsActivity::class.java)
            intent.putExtra("user_id", userId)
            startActivity(intent)
        }

        imageViewBack.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("user_id", userId)
            startActivity(intent)
        }
    }

    private fun loadSettings(userId: Long) {
        nameTextView.text = databaseHelper.getUserNameById(userId)
        loginTextView.text = databaseHelper.getUserLoginById(userId)
        switchStartOfPeriod.isChecked = databaseHelper.isCycleStartEnabled(userId)
        switchOvulation.isChecked = databaseHelper.isOvulationEnabled(userId)
        switchRememberMe.isChecked = databaseHelper.isRememberMeEnabled(userId)
        switchPeriodInDays.isChecked = databaseHelper.isPeriodEnabled(userId)
    }

    private fun saveRememberMePreference(isChecked: Boolean) {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("remember_me", isChecked)
        editor.apply()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // Создаем канал уведомлений (необходим для Android 8.0 и выше)
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Настройки уведомлений"
            val descriptionText = "Уведомления об изменении настроек"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Метод для планирования уведомлений
    @RequiresApi(Build.VERSION_CODES.O)
    private fun scheduleNotification(notificationType: String, userId: Long) {
        // Получаем дату следующего цикла или овуляции
        val nextCycleDate = LocalDate.now().plusDays(1) // Замените на реальную дату следующего цикла
        val notificationReceiver = PeriodNotificationReceiver()

        when (notificationType) {
            "start_cycle" -> notificationReceiver.schedulePeriodNotification(this, userId, nextCycleDate, "start_cycle")
            "ovulation" -> notificationReceiver.schedulePeriodNotification(this, userId, nextCycleDate, "ovulation")
            "period_reminder" -> notificationReceiver.schedulePeriodNotification(this, userId, nextCycleDate, "period_reminder")
        }
    }

    // Метод для отмены уведомлений
    private fun cancelNotification(userId: Long) {
        val intent = Intent(this, PeriodNotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            userId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent) // Отменяем уведомление
    }
}
