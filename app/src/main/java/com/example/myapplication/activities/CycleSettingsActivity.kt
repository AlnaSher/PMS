package com.example.myapplication.activities

import com.example.myapplication.database.DatabaseHelper
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import com.example.myapplication.R
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class CycleSettingsActivity : ComponentActivity() {

    private lateinit var editTextCycleLength: EditText
    private lateinit var editTextDuration: EditText
    private lateinit var textViewSelectedDate: TextView
    private lateinit var buttonSave: Button
    private var selectedDate: Calendar = Calendar.getInstance() // Для хранения выбранной даты
    private lateinit var databaseHelper: DatabaseHelper
    private var userId: Long = -1 // Идентификатор пользователя

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val isTablet = resources.getBoolean(R.bool.is_tablet)
        if (!isTablet) {
            // Если это телефон, фиксируем ориентацию в портретной
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        setContentView(R.layout.activity_cycle_settings)

        // Инициализация UI элементов
        editTextCycleLength = findViewById(R.id.editTextCycleLength)
        editTextDuration = findViewById(R.id.editTextDuration)
        textViewSelectedDate = findViewById(R.id.textViewSelectedDate)
        buttonSave = findViewById(R.id.buttonSave)

        // Инициализация базы данных
        databaseHelper = DatabaseHelper(this)

        userId = intent.getLongExtra("user_id", -1)

        // Загрузка настроек из базы данных, если они есть
        loadSettingsIfAvailable()

        // Установка обработчика для выбора даты
        textViewSelectedDate.setOnClickListener {
            showDatePickerDialog()
        }

        // Установка обработчика для кнопки "Сохранить"
        buttonSave.setOnClickListener {
            saveSettings()
        }
    }

    // Метод для загрузки настроек из базы данных, если они существуют
    private fun loadSettingsIfAvailable() {
        val settings = databaseHelper.getCycleSettings(userId) // Получаем настройки в виде Map

        if (settings != null) {
            // Извлекаем значения из Map и приводим к нужным типам
            val cycleLength = settings["cycleLength"] as? Int
            val duration = settings["duration"] as? Int
            val lastPeriodDate = settings["lastPeriodDate"] as? String

            // Проверяем, что все значения успешно получены
            if (cycleLength != null && duration != null && lastPeriodDate != null) {
                editTextCycleLength.setText(cycleLength.toString())
                editTextDuration.setText(duration.toString())
                textViewSelectedDate.text = lastPeriodDate

                // Обновляем выбранную дату для DatePickerDialog
                val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                selectedDate.time = dateFormat.parse(lastPeriodDate) ?: Calendar.getInstance().time
            }
        }
    }



    // Метод для отображения диалога выбора даты
    private fun showDatePickerDialog() {
        val year = selectedDate.get(Calendar.YEAR)
        val month = selectedDate.get(Calendar.MONTH)
        val day = selectedDate.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDayOfMonth ->
            // Обновляем выбранную дату
            selectedDate.set(selectedYear, selectedMonth, selectedDayOfMonth)
            updateSelectedDateTextView()
        }, year, month, day)

        datePickerDialog.show()
    }

    // Обновление текстового поля с выбранной датой
    private fun updateSelectedDateTextView() {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        textViewSelectedDate.text = dateFormat.format(selectedDate.time)
    }

    // Метод для сохранения настроек и автоматического заполнения дней с менструацией
    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveSettings() {
        val cycleLength = editTextCycleLength.text.toString()
        val duration = editTextDuration.text.toString()
        val lastPeriodDate = textViewSelectedDate.text.toString()

        // Проверяем, заполнены ли все поля
        if (cycleLength.isEmpty() || duration.isEmpty() || lastPeriodDate == "Дата не выбрана") {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }

        // Сохраняем данные о цикле в базу данных
        val isSaved = databaseHelper.addOrUpdateCycleSettings(userId, cycleLength.toInt(), duration.toInt(), lastPeriodDate)

        if (isSaved != -1L) {
            // Успешное сохранение настроек
            Toast.makeText(this, "Настройки сохранены", Toast.LENGTH_SHORT).show()

            // Заполнение дней с менструацией в базе данных
            addMenstrualDaysToDatabase(cycleLength.toInt(), duration.toInt(), lastPeriodDate)

            // Переход на страницу профиля
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("user_id", userId) // Передача ID
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "Ошибка сохранения настроек", Toast.LENGTH_SHORT).show()
        }
    }

    // Метод для автоматического заполнения дней с менструацией
    @RequiresApi(Build.VERSION_CODES.O)
    private fun addMenstrualDaysToDatabase(cycleLength: Int, duration: Int, lastPeriodDate: String) {
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val startDate = Calendar.getInstance().apply {
            time = sdf.parse(lastPeriodDate) ?: return
        }

        // Проверка на наличие записей о менструации
        val existingMenstrualDays = databaseHelper.getMenstrualDays(userId)
        if (existingMenstrualDays.isNotEmpty()) {
            Log.d("CycleSettings", "Menstrual days already recorded in the database.")
            return
        }

        val today = LocalDate.now()
        for (i in 0 until cycleLength) {
            // Преобразуем текущую дату из Calendar в LocalDate
            val dateString = sdf.format(startDate.time)
            val dateLocalDate = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("dd.MM.yyyy"))

            // Прекращаем добавление, если достигли сегодняшней даты
            if (dateLocalDate.isAfter(today)) break

            val dayOfCycle = i + 1
            databaseHelper.addOrUpdateDayStatus(userId, dateLocalDate, "Менструация", dayOfCycle)

            // Переход к следующему дню
            startDate.add(Calendar.DAY_OF_MONTH, 1)
        }

        Log.d("CycleSettings", "Menstrual days added to the database.")
    }
}
