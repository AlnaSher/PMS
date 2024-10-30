package com.example.myapplication.adapters

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.database.DatabaseHelper
import com.example.myapplication.helperClasses.CalendarDay
import com.example.myapplication.helperClasses.DayStatus
import java.time.LocalDate

class CalendarAdapter(
    private val context: Context, // Добавьте контекст как параметр
    private val onDayClick: (CalendarDay) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.DayViewHolder>() {

    internal var days: List<CalendarDay> = emptyList()
    private var selectedDay: CalendarDay? = null  // Храним выбранный день
    private val databaseHelper: DatabaseHelper = DatabaseHelper(context) // Инициализируем DatabaseHelper

    // Метод для обновления списка дней
    fun submitList(newDays: List<CalendarDay>) {
        days = newDays
        notifyDataSetChanged()
    }

    // Метод для получения элемента по позиции
    fun getItem(position: Int): CalendarDay {
        return days[position]
    }

    // Метод для получения выбранного дня
    fun getSelectedDay(): CalendarDay? {
        return selectedDay
    }

    // Новый метод для выбора дня по дате
    fun selectDay(date: LocalDate) {
        selectedDay = days.find { it.date == date } // Найти день по дате
        notifyDataSetChanged()  // Обновить адаптер
    }

    // Метод для выбора дня
    fun setSelectedDay(day: CalendarDay) {
        selectedDay = day
        notifyDataSetChanged()  // Перерисовать список для отображения выделенного дня
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
        return DayViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val day = days[position]
        holder.bind(day, day == selectedDay)  // Передаем флаг, является ли день выбранным
    }

    override fun getItemCount(): Int = days.size

    inner class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(android.R.id.text1)

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(day: CalendarDay, isSelected: Boolean) {
            textView.text = day.date.dayOfMonth.toString()

            // Выбор цвета в зависимости от статуса дня
            when (day.status) {
                DayStatus.MENSTRUATION -> textView.setBackgroundColor(ContextCompat.getColor(itemView.context, android.R.color.holo_red_light))
                DayStatus.OVULATION -> textView.setBackgroundColor(ContextCompat.getColor(itemView.context, android.R.color.holo_blue_light))
                DayStatus.HIGH_FERTILITY -> textView.setBackgroundColor(ContextCompat.getColor(itemView.context, android.R.color.holo_green_light))
                else -> textView.setBackgroundColor(ContextCompat.getColor(itemView.context, android.R.color.transparent))
            }

            // Если день выбран, выделяем его
            if (isSelected) {
                textView.setBackgroundColor(ContextCompat.getColor(itemView.context, android.R.color.holo_orange_light))
            }

            // Обработка клика по дню
            itemView.setOnClickListener {
                setSelectedDay(day)  // Обновляем выбранный день
                onDayClick(day)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun fillCalendarWithStatuses(
        userId: Long,
        startDate: LocalDate,
        cycleLength: Int,
        menstruationDuration: Int,
        lastPeriodDate: LocalDate
    ) {
        val days = mutableListOf<CalendarDay>()

        // Заполняем только 7 дней (с понедельника по воскресенье)
        for (i in 0 until 7) {
            val date = startDate.plusDays(i.toLong())
            val day = CalendarDay(date)

            // Получаем статус дня из базы данных для указанного userId, если он существует
            val savedStatus = databaseHelper.getDayStatus(userId, date) // Передача userId

            if (savedStatus != null) {
                // Если статус найден, устанавливаем его
                day.status = DayStatus.valueOf(savedStatus)
            } else {
                // Если статус не найден, определяем статус и порядковый номер на основе цикла
                val cycleDayNumber = calculateCycleDayNumber(date, lastPeriodDate, cycleLength)
                day.cycleDayNumber = cycleDayNumber

                // Определяем статус на основе цикла
                day.status = if (cycleDayNumber in 1..menstruationDuration) {
                    DayStatus.MENSTRUATION
                } else {
                    DayStatus.NONE // Или другой статус по вашему выбору
                }
            }
            days.add(day)
        }

        // Отправляем дни в адаптер календаря
        submitList(days)
    }

    // Вспомогательная функция для расчета порядкового номера дня в цикле
    @RequiresApi(Build.VERSION_CODES.O)
    private fun calculateCycleDayNumber(date: LocalDate, lastPeriodDate: LocalDate, cycleLength: Int): Int {
        val daysSinceLastPeriod = date.toEpochDay() - lastPeriodDate.toEpochDay()
        return ((daysSinceLastPeriod % cycleLength) + 1).toInt()
    }
}
