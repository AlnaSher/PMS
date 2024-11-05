package com.example.myapplication.activities

import android.annotation.SuppressLint
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
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar

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

        val cycleSettings = databaseHelper.getCycleSettings(userId);
        val duration = cycleSettings?.get("duration") as Int

        // Установка слушателей на переключатели с отправкой уведомлений
        switchStartOfPeriod.setOnCheckedChangeListener { _, isChecked ->
            databaseHelper.updateCycleStartEnabled(userId, isChecked)
            if (isChecked) schedulePeriodNotification(this@CommonSettingsViewActivity, userId, "start_cycle", duration)
            else cancelNotification(userId) // Отменяем уведомление, если выключено
            showToast("Настройка 'Начало месячных' сохранена")
        }

        switchOvulation.setOnCheckedChangeListener { _, isChecked ->
            databaseHelper.updateOvulationEnabled(userId, isChecked)
            if (isChecked) schedulePeriodNotification(this@CommonSettingsViewActivity, userId, "ovulation", duration)
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
            if (isChecked) schedulePeriodNotification(this@CommonSettingsViewActivity, userId, "period_reminder", duration)
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

    @SuppressLint("ScheduleExactAlarm")
    @RequiresApi(Build.VERSION_CODES.O)
    fun schedulePeriodNotification(context: Context, userId: Long, notificationType: String, duration: Int) {
        val dbHelper = DatabaseHelper(context)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val today = LocalDate.now()

        val notificationTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 12)  // Устанавливаем время уведомления на 12:00 дня
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        // Получаем дату последнего начала месячных или овуляции в зависимости от типа уведомления
        val targetDate = when (notificationType) {
            "start_cycle" -> {
                // Получаем последнюю дату начала месячных
                val lastPeriodStartDate = dbHelper.getLastPeriodStartDate(userId) ?: return
                lastPeriodStartDate.plusDays(duration.toLong()) // Предполагаемая дата начала следующих месячных
            }
            "period_reminder" -> {
                // Дата последних месячных + duration, но с уведомлением за три дня до
                val lastPeriodStartDate = dbHelper.getLastPeriodStartDate(userId) ?: return
                lastPeriodStartDate.plusDays(duration.toLong()).minusDays(3) // За три дня до начала
            }
            "ovulation" -> {
                // Получаем последнюю дату овуляции
                val lastOvulationDate = dbHelper.getLastOvulationDate(userId)
                val nextOvulationDate = if (lastOvulationDate == null || lastOvulationDate.isBefore(today)) {
                    lastOvulationDate?.plusDays(duration.toLong())
                } else {
                    lastOvulationDate
                } ?: return
                nextOvulationDate.minusDays(1) // Уведомление за день до предполагаемой овуляции
            }
            else -> return
        }

        // Устанавливаем время срабатывания уведомления
        notificationTime.timeInMillis = targetDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        // Создаем Intent для передачи уведомления
        val intent = Intent(context, PeriodNotificationReceiver::class.java).apply {
            putExtra("notification_type", notificationType)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            userId.toInt(), // Уникальный ID для пользователя
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Устанавливаем точное срабатывание уведомления в указанное время
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, notificationTime.timeInMillis, pendingIntent)
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
