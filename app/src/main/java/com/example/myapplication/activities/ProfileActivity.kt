package com.example.myapplication.activities

import android.os.Build
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import com.example.myapplication.R
import com.example.myapplication.adapters.CalendarAdapter
import com.example.myapplication.helperClasses.CalendarDay
import com.example.myapplication.helperClasses.DayStatus
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.*

class ProfileActivity : ComponentActivity() {

    private var currentWeekOffset = 0
    private lateinit var adapter: CalendarAdapter
    private lateinit var gestureDetector: GestureDetector
    private lateinit var recyclerViewCalendar: RecyclerView
    private lateinit var textViewMonthYear: TextView

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Инициализация элементов интерфейса
        recyclerViewCalendar = findViewById(R.id.recyclerViewCalendar)
        textViewMonthYear = findViewById(R.id.textViewMonthYear)

        // Инициализация адаптера
        adapter = CalendarAdapter { day -> showDayInfo(day) }
        recyclerViewCalendar.layoutManager = GridLayoutManager(this, 7) // 7 колонок для дней недели
        recyclerViewCalendar.adapter = adapter

        // Показать текущую неделю
        showWeek()

        // Настраиваем жесты для свайпов и двойного нажатия
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
                    showWeek()
                    return true
                }
                return false
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                // Получаем координаты нажатия
                e?.let { handleDoubleTap(it) }
                return super.onDoubleTap(e)
            }
        })

        // Обработчик касаний для RecyclerView
        recyclerViewCalendar.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }
    }

    // Показать дни текущей недели
    @RequiresApi(Build.VERSION_CODES.O)
    private fun showWeek() {
        val currentDate = LocalDate.now().plusWeeks(currentWeekOffset.toLong())

        // Найдем начало и конец недели
        val firstDayOfWeek = currentDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val lastDayOfWeek = firstDayOfWeek.plusDays(6)

        // Отобразить текущий месяц и год
        val monthYearFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
        textViewMonthYear.text = firstDayOfWeek.format(monthYearFormatter)

        // Получить дни недели
        val daysInWeek = generateDaysForWeek(firstDayOfWeek)
        adapter.submitList(daysInWeek)
    }

    // Обработчик двойного нажатия
    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleDoubleTap(event: MotionEvent) {
        val x = event.x
        val y = event.y

        // Получаем позицию нажатия
        val position = recyclerViewCalendar.getChildAdapterPosition(recyclerViewCalendar.findChildViewUnder(x, y) ?: return)

        if (position != RecyclerView.NO_POSITION) {
            val day = adapter.getItem(position)
            markMenstruation(day)
        }
    }

    // Отметить месячные
    @RequiresApi(Build.VERSION_CODES.O)
    private fun markMenstruation(day: CalendarDay) {
        // Изменяем статус дня на "менструация"
        day.status = DayStatus.MENSTRUATION
        // Обновляем статус в адаптере
        val position = adapter.days.indexOf(day) // Получаем позицию дня в списке
        adapter.notifyItemChanged(position) // Уведомляем адаптер об изменении
        Toast.makeText(this, "Менструация отмечена для ${day.date.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))}", Toast.LENGTH_SHORT).show()
    }

    // Отображение информации о выбранном дне
    @RequiresApi(Build.VERSION_CODES.O)
    private fun showDayInfo(day: CalendarDay) {
        textViewMonthYear.text = "Вы выбрали: ${day.date.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))} - Статус: ${day.status}"
    }

    // Генерация дней для текущей недели
    @RequiresApi(Build.VERSION_CODES.O)
    private fun generateDaysForWeek(startOfWeek: LocalDate): List<CalendarDay> {
        val days = mutableListOf<CalendarDay>()

        // Генерация дней недели с их статусами
        for (i in 0..6) {
            val currentDay = startOfWeek.plusDays(i.toLong())
            val status = calculateDayStatus(currentDay)
            days.add(CalendarDay(currentDay, status))
        }

        return days
    }

    // Пример вычисления статуса дня
    @RequiresApi(Build.VERSION_CODES.O)
    private fun calculateDayStatus(date: LocalDate): DayStatus {
        // Простая логика для назначения статусов
        return when {
            date.dayOfMonth % 10 == 0 -> DayStatus.MENSTRUATION // Менструация каждые 10 дней (пример)
            date.dayOfMonth % 15 == 0 -> DayStatus.OVULATION // Овуляция каждые 15 дней (пример)
            date.dayOfMonth % 8 == 0 -> DayStatus.HIGH_FERTILITY // Высокая фертильность каждые 8 дней
            else -> DayStatus.NONE
        }
    }
}