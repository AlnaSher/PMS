package com.example.myapplication.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.myapplication.R
import com.example.myapplication.helperClasses.Cycle

class CycleAdapter(
    context: Context,
    private val cycles: List<Cycle>
) : ArrayAdapter<Cycle>(context, 0, cycles) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Get the current cycle item
        val cycle = getItem(position)

        // Use an existing view if possible, or create a new one
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_cycle, parent, false)

        // Find the TextViews in the item layout
        val startDateTextView = view.findViewById<TextView>(R.id.startDateTextView)
        val endDateTextView = view.findViewById<TextView>(R.id.endDateTextView)
        val durationTextView = view.findViewById<TextView>(R.id.durationTextView)
        val symptomsTextView = view.findViewById<TextView>(R.id.symptomsTextView)

        // Set the data to each TextView
        if (cycle != null) {
            startDateTextView.text = "Начало: ${cycle.startDate}"
            endDateTextView.text = "Конец: ${cycle.endDate}"
            durationTextView.text = "Продолжительность: ${cycle.length} дней"
            symptomsTextView.text = "Симптомы: ${cycle.symptoms}"
        }

        return view
    }
}
