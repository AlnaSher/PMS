package com.example.myapplication.helperClasses

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate

class CalendarDay(
    val date: LocalDate,
    var status: DayStatus = DayStatus.NONE,
    var cycleDayNumber: Int = 0 // Добавлено поле для порядкового номера в цикле
) {
    fun updateStatus(newStatus: DayStatus) {
        status = newStatus
    }

    override fun toString(): String {
        return "CalendarDay(date=$date, status=$status, cycleDayNumber=$cycleDayNumber)"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setStatusFromCycleData(cycleLength: Int, menstruationDuration: Int, lastPeriodDate: LocalDate) {
        val daysSinceLastPeriod = date.toEpochDay() - lastPeriodDate.toEpochDay()
        cycleDayNumber = ((daysSinceLastPeriod % cycleLength) + 1).toInt()

        status = when {
            cycleDayNumber in 1..menstruationDuration -> DayStatus.MENSTRUATION
            cycleDayNumber == cycleLength / 2 -> DayStatus.OVULATION
            cycleDayNumber > menstruationDuration && cycleDayNumber < cycleLength / 2 -> DayStatus.HIGH_FERTILITY
            else -> DayStatus.NONE
        }
    }
}
class DayStatus private constructor(val description: String) {

    companion object {
        val NONE = DayStatus("Нет событий")
        val MENSTRUATION = DayStatus("Менструация")
        val OVULATION = DayStatus("Овуляция")
        val DELAY = DayStatus("Задержка")
        val MENSTRUATION_PREDICT = DayStatus("Прогноз на менструацию")
        val OVULATION_PREDICT = DayStatus("Прогноз на овуляцию")
        val HIGH_FERTILITY = DayStatus("Фертильность")

        // Реализация valueOf с обработкой ошибок
        fun valueOf(value: String?): DayStatus {
            // Проверка на null или пустое значение
            if (value.isNullOrEmpty()) {
                throw IllegalArgumentException("DayStatus value cannot be null or empty")
            }

            return when (value.trim()) {  // Используем trim() для удаления лишних пробелов
                NONE.description -> NONE
                MENSTRUATION.description -> MENSTRUATION
                OVULATION.description -> OVULATION
                DELAY.description -> DELAY
                MENSTRUATION_PREDICT.description -> MENSTRUATION_PREDICT
                OVULATION_PREDICT.description -> OVULATION_PREDICT
                HIGH_FERTILITY.description -> HIGH_FERTILITY
                else -> {
                    // Логирование неизвестного значения для отладки
                    println("Warning: Unknown DayStatus: $value")
                    throw IllegalArgumentException("Unknown DayStatus: $value")
                }
            }
        }
    }

    override fun toString(): String {
        return description
    }
}
