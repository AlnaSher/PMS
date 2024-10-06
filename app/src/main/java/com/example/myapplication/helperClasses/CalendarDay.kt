package com.example.myapplication.helperClasses

import java.time.LocalDate

// Класс для представления дня с датой и статусом
data class CalendarDay(
    val date: LocalDate,
    var status: DayStatus = DayStatus.NONE
)

// Статусы для дней
enum class DayStatus {
    NONE,              // Нет событий
    MENSTRUATION,      // День менструации
    OVULATION,         // День овуляции
    DELAY,             // Задержка
    MENSTRUATION_PREDICT,  // Прогноз на менструацию
    OVULATION_PREDICT, // Прогноз на овуляцию
    HIGH_FERTILITY     // День высокой фертильности
}