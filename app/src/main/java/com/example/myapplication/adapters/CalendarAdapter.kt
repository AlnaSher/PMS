package com.example.myapplication.adapters

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.helperClasses.CalendarDay
import com.example.myapplication.helperClasses.DayStatus

class CalendarAdapter(private val onDayClick: (CalendarDay) -> Unit) : RecyclerView.Adapter<CalendarAdapter.DayViewHolder>() {

    internal var days: List<CalendarDay> = emptyList()

    fun submitList(newDays: List<CalendarDay>) {
        days = newDays
        notifyDataSetChanged()
    }

    // Метод для получения элемента по позиции
    fun getItem(position: Int): CalendarDay {
        return days[position]
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
        return DayViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val day = days[position]
        holder.bind(day)
    }

    override fun getItemCount(): Int = days.size

    inner class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(android.R.id.text1)

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(day: CalendarDay) {
            textView.text = day.date.dayOfMonth.toString()

            // Выбор цвета в зависимости от статуса дня
            when (day.status) {
                DayStatus.MENSTRUATION -> textView.setBackgroundColor(ContextCompat.getColor(itemView.context, android.R.color.holo_red_light))
                DayStatus.OVULATION -> textView.setBackgroundColor(ContextCompat.getColor(itemView.context, android.R.color.holo_blue_light))
                DayStatus.HIGH_FERTILITY -> textView.setBackgroundColor(ContextCompat.getColor(itemView.context, android.R.color.holo_green_light))
                else -> textView.setBackgroundColor(ContextCompat.getColor(itemView.context, android.R.color.transparent))
            }

            // Обработка клика по дню
            itemView.setOnClickListener {
                onDayClick(day)
            }
        }
    }
}