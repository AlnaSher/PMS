package com.example.myapplication.activities

import com.example.myapplication.adapters.CalendarAdapter
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.database.DatabaseHelper
import com.example.myapplication.helperClasses.CalendarDay
import com.example.myapplication.helperClasses.Cycle
import com.example.myapplication.helperClasses.DayStatus
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.TemporalAdjusters
import java.util.*

class ProfileActivity : ComponentActivity() {

    private lateinit var databaseHelper: DatabaseHelper
    @RequiresApi(Build.VERSION_CODES.O)
    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.getDefault())


    private var currentWeekOffset = 0
    private lateinit var adapter: CalendarAdapter
    private lateinit var gestureDetector: GestureDetector
    private lateinit var recyclerViewCalendar: RecyclerView
    private lateinit var textViewMonthYear: TextView
    private lateinit var textViewCycleStats: TextView
    private lateinit var textViewSymptomForecast: TextView

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val isTablet = resources.getBoolean(R.bool.is_tablet)
        if (!isTablet) {
            // Если это телефон, фиксируем ориентацию в портретной
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        setContentView(R.layout.activity_profile)

        val userId = intent.getLongExtra("user_id", -1)

        databaseHelper = DatabaseHelper(this)

        // Инициализация элементов интерфейса
        recyclerViewCalendar = findViewById(R.id.recyclerViewCalendar)
        textViewMonthYear = findViewById(R.id.textViewMonthYear)
        textViewSymptomForecast = findViewById(R.id.textViewSymptomForecast)

        // Кнопки интерфейса
        val buttonMarkPeriod: Button = findViewById(R.id.buttonMarkPeriod)
        val buttonMarkSymptoms: Button = findViewById(R.id.buttonMarkSymptoms)
        val buttonFullStatistics: Button = findViewById(R.id.buttonFullStatistics)
        val imageViewSettings = findViewById<ImageView>(R.id.imageViewSettings)
        imageViewSettings.setImageResource(R.drawable.free_icon_setting_2040504)

        imageViewSettings.setOnClickListener {
            val intent = Intent(this, CommonSettingsViewActivity::class.java)
            intent.putExtra("user_id", userId) // Передача ID
            startActivity(intent)
        }

        // Инициализация адаптера
        adapter = CalendarAdapter(this) { day -> showDayInfo(day) }
        recyclerViewCalendar.layoutManager = GridLayoutManager(this, 7) // 7 колонок для дней недели
        recyclerViewCalendar.adapter = adapter

        loadCycleData(userId)

        // Показать текущую неделю
        showWeek(userId)
        selectToday()

        // Настройка кнопок
        buttonMarkPeriod.setOnClickListener {
            markPeriodForSelectedDay(userId) // Изменено для передачи userId
        }

        buttonMarkSymptoms.setOnClickListener {
            markSymptomsForSelectedDay(userId)
        }

        buttonFullStatistics.setOnClickListener {
            showFullStatistics(userId)
        }

        // Настраиваем жесты для свайпов
        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            private val SWIPE_THRESHOLD = 100
            private val SWIPE_VELOCITY_THRESHOLD = 100

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                val diffX = e2.x - (e1?.x ?: 0f)
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                        // Свайп вправо (предыдущая неделя)
                        currentWeekOffset--
                    } else {
                        // Свайп влево (следующая неделя)
                        currentWeekOffset++
                    }
                    showWeek(userId)
                    return true
                }
                return false
            }
        })

        // Обработчик касаний для RecyclerView
        recyclerViewCalendar.setOnTouchListener { _, event ->
            if (gestureDetector.onTouchEvent(event)) {
                // Gesture detector handled the event (swipe), return true to consume it.
                true
            } else {
                // Gesture detector did not handle the event, pass it to the recyclerView (calendar).
                recyclerViewCalendar.performClick()  // Optionally trigger a click event on RecyclerView
                false  // Allow other events to be processed (clicks, etc.)
            }
        }

        // Покажем прогноз симптомов на сегодня
        showSymptomForecast(LocalDate.now())
    }

    // Выбрать сегодняшний день
    @RequiresApi(Build.VERSION_CODES.O)
    private fun selectToday() {
        val today = LocalDate.now()
        adapter.selectDay(today) // Добавьте метод в адаптер для выбора дня
        showDayInfo(adapter.getSelectedDay() ?: return) // Показываем информацию о выбранном дне
    }

    // Показать дни текущей недели
    @RequiresApi(Build.VERSION_CODES.O)
    private fun showWeek(userId: Long) {
        val currentDate = LocalDate.now().plusWeeks(currentWeekOffset.toLong())

        // Найдем начало недели
        val firstDayOfWeek = currentDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

        // Отобразить текущий месяц и год
        val monthYearFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
        textViewMonthYear.text = firstDayOfWeek.format(monthYearFormatter)

        // Получить дни недели с их статусами из базы данных
        val daysInWeek = loadDayStatusesForWeek(userId, firstDayOfWeek)
        adapter.submitList(daysInWeek)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadDayStatusesForWeek(userId: Long, startOfWeek: LocalDate): List<CalendarDay> {
        val dbHelper = DatabaseHelper(this)
        val days = mutableListOf<CalendarDay>()

        // Проходим по дням недели
        for (i in 0..6) {
            val currentDay = startOfWeek.plusDays(i.toLong())
            val statusString: String? = dbHelper.getDayStatus(userId, currentDay) // Получаем строку статуса дня

            // Если statusString не найден, задаем значение по умолчанию NONE
            val status: DayStatus = statusString?.let { DayStatus.valueOf(it) } ?: DayStatus.NONE

            days.add(CalendarDay(currentDay, status))
        }

        return days
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun markPeriodForSelectedDay(userId: Long) {
        val selectedDay = adapter.getSelectedDay() ?: run {
            Toast.makeText(this, "Выберите день", Toast.LENGTH_SHORT).show()
            return
        }

        val dbHelper = DatabaseHelper(this)

        // Получаем настройки цикла для данного пользователя
        val cycleSettings = dbHelper.getCycleSettings(userId)
        if (cycleSettings == null) {
            Toast.makeText(this, "Настройки цикла не найдены", Toast.LENGTH_SHORT).show()
            return
        }

        // Извлекаем параметры цикла
        val cycleLength = cycleSettings["cycleLength"] as? Int ?: run {
            Toast.makeText(this, "Некорректная длина цикла", Toast.LENGTH_SHORT).show()
            return
        }

        val lastPeriodDateString = cycleSettings["lastPeriodDate"] as? String ?: run {
            Toast.makeText(this, "Некорректная дата последней менструации", Toast.LENGTH_SHORT).show()
            return
        }

        val duration = cycleSettings["duration"] as? Int ?: run {
            Toast.makeText(this, "Некорректная длина месячных", Toast.LENGTH_SHORT).show()
            return
        }

        // Устанавливаем статус дня и сохраняем его в базе данных
        selectedDay.status = DayStatus.MENSTRUATION
        dbHelper.addOrUpdateDayStatus(userId, selectedDay.date, selectedDay.status.description, 1) // Помечаем текущий день как день менструации

        // Проверяем, был ли отмечен предыдущий день как менструация
        val previousDay = selectedDay.date.minusDays(1)
        if (!dbHelper.isDayMarkedAsMenstruation(userId, previousDay)) {
            // Если предыдущий день не отмечен, то сохраняем прошлый цикл и добавляем день овуляции

            // Определяем дату окончания предыдущего цикла
            var endDate = previousDay

            // Находим последний день предыдущего цикла
            while (dbHelper.isDayMarkedAsMenstruation(userId, endDate)) {
                endDate = endDate.minusDays(1)
            }
            val endDateString = endDate.format(dateFormatter)

            // Получаем все уникальные симптомы между startDate и endDate
            val symptomsSet = mutableSetOf<String>()
            val symptomsRecords = dbHelper.getSymptomsInCycle(userId, endDateString, selectedDay.date.toString())
            for (symptom in symptomsRecords) {
                symptomsSet.add(symptom)
            }
            val symptoms = symptomsSet.joinToString(", ")

            // Создаем и сохраняем цикл
            val cycle = Cycle(
                id = userId,
                startDate = lastPeriodDateString,
                endDate = endDateString,
                length = cycleLength,
                symptoms = symptoms
            )
            dbHelper.insertCycle(cycle, userId)

            // Устанавливаем день овуляции
            val ovulationDay = selectedDay.date.plusDays(14) // 14-й день от начала нового цикла
            if (!dbHelper.isDayMarkedAsOvulation(userId, ovulationDay.toString())) {
                dbHelper.addOrUpdateDayStatus(userId, ovulationDay, DayStatus.OVULATION.description, 1) // Помечаем день овуляции
            }

            Toast.makeText(this, "Месячные отмечены, овуляция добавлена, цикл сохранен", Toast.LENGTH_SHORT).show()

        } else {
            // Если предыдущий день уже отмечен как менструация, просто сохраняем текущий день
            Toast.makeText(this, "Месячные отмечены и сохранены", Toast.LENGTH_SHORT).show()
        }

        // Обновляем адаптер
        adapter.notifyDataSetChanged()
    }







    @RequiresApi(Build.VERSION_CODES.O)
    fun calculateCycleDayNumber(date: LocalDate, lastPeriodDate: LocalDate, cycleLength: Int): Int {
        val daysSinceLastPeriod = date.toEpochDay() - lastPeriodDate.toEpochDay()

        // Приводим daysSinceLastPeriod к положительному значению для правильного расчета
        val normalizedDays = (daysSinceLastPeriod % cycleLength).toInt()

        return if (normalizedDays < 0) {
            // Если результат отрицательный, корректируем его, добавляя длину цикла
            normalizedDays + cycleLength
        } else {
            normalizedDays + 1
        }
    }

    // Отметить симптомы для выбранного дня
    private val REQUEST_CODE_SELECT_SYMPTOMS = 1001
    private val EXTRA_SELECTED_DAY = "selected_day"

    @RequiresApi(Build.VERSION_CODES.O)
    private fun markSymptomsForSelectedDay(userId: Long) {
        val selectedDay = adapter.getSelectedDay() ?: run {
            Toast.makeText(this, "Выберите день", Toast.LENGTH_SHORT).show()
            return
        }

        // Start the activity for selecting symptoms, passing the selected day
        val intent = Intent(this, SymptomSelectionActivity::class.java)
        intent.putExtra(EXTRA_SELECTED_DAY, selectedDay.date.toString())  // Pass the selected date as a string
        intent.putExtra("user_id", userId)  // Передаем userId
        startActivity(intent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_SELECT_SYMPTOMS && resultCode == RESULT_OK) {
            val selectedSymptoms = data?.getStringArrayListExtra("selected_symptoms") ?: return
            val selectedDayString = data.getStringExtra(EXTRA_SELECTED_DAY) ?: return
            val selectedDay = LocalDate.parse(selectedDayString)

            // Find the selected day in the adapter and update its symptoms
            val calendarDay = adapter.days.find { it.date == selectedDay }
            calendarDay?.let {
                it.status = DayStatus.HIGH_FERTILITY  // Example: set high fertility or use symptoms as the status
                // Save the symptoms (this is where you'd implement saving the symptoms for that day)
                adapter.notifyDataSetChanged()
            }

            Toast.makeText(this, "Симптомы для ${selectedDay} обновлены", Toast.LENGTH_SHORT).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadCycleData(userId: Long) {
        // Создаем экземпляр DatabaseHelper
        val dbHelper = DatabaseHelper(this)

        // Получаем настройки цикла для данного пользователя
        val cycleSettings = dbHelper.getCycleSettings(userId)

        // Проверяем, успешно ли получены настройки
        if (cycleSettings != null) {
            try {
                val cycleLength = cycleSettings["cycleLength"] as Int
                val menstruationDuration = cycleSettings["duration"] as Int
                val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
                val lastPeriodDate = LocalDate.parse(cycleSettings["lastPeriodDate"] as String, formatter)

                // Находим ближайший понедельник к текущей дате
                val today = LocalDate.now()
                val startOfWeek = today.minusDays((today.dayOfWeek.value - 1).toLong()) // Найти понедельник

                // Заполняем календарь с понедельника по воскресенье
                adapter.fillCalendarWithStatuses(userId, startOfWeek, cycleLength, menstruationDuration, lastPeriodDate)

            } catch (e: DateTimeParseException) {
                println("Date parsing failed: ${e.message}")
            }
            // После получения данных заполняем календарь
        } else {
            // Обработка случая, если настройки не найдены
            Toast.makeText(this, "Настройки цикла не найдены", Toast.LENGTH_SHORT).show()
        }
    }

    // Переход на экран полной статистики
    private fun showFullStatistics(userId: Long) {
        // Открытие другой Activity для статистики
        val intent = Intent(this, StatisticsActivity::class.java)
        intent.putExtra("user_id", userId) // Передача ID
        startActivity(intent)
    }

    // Отображение информации о выбранном дне
    @RequiresApi(Build.VERSION_CODES.O)
    private fun showDayInfo(day: CalendarDay) {
        textViewMonthYear.text = "${day.date.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))} - ${day.status}"
    }

    // Прогноз симптомов на сегодня
    @RequiresApi(Build.VERSION_CODES.O)
    private fun showSymptomForecast(currentDate: LocalDate) {
        textViewSymptomForecast.text = "Прогноз симптомов на ${currentDate.format(dateFormatter)}"
    }
}
