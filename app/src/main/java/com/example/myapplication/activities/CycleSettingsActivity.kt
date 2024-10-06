package com.example.myapplication.activities

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.myapplication.database.DatabaseHelper
import com.example.myapplication.R
import java.util.Calendar

class CycleSettingsActivity : ComponentActivity() {
    private lateinit var editTextDuration: EditText
    private lateinit var editTextCycleLength: EditText
    private lateinit var textViewSelectedDate: TextView
    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cycle_settings)

        editTextDuration = findViewById(R.id.editTextDuration)
        editTextCycleLength = findViewById(R.id.editTextCycleLength)
        textViewSelectedDate = findViewById(R.id.textViewSelectedDate)
        val buttonSave = findViewById<Button>(R.id.buttonSave) // Убедитесь, что у вас есть такая кнопка

        databaseHelper = DatabaseHelper(this)

        // Устанавливаем слушатель нажатия на поле с датой
        textViewSelectedDate.setOnClickListener {
            showDatePickerDialog()
        }

        // Обработка нажатия на кнопку "Сохранить"
        buttonSave.setOnClickListener {
            saveCycleSettings()
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                textViewSelectedDate.text = selectedDate
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    private fun saveCycleSettings() {
        val duration = editTextDuration.text.toString().toIntOrNull()
        val cycleLength = editTextCycleLength.text.toString().toIntOrNull()
        val selectedDate = textViewSelectedDate.text.toString()

        // Предположим, что у вас есть идентификатор текущего пользователя
        val currentUserId = 1 // Замените это на реальный способ получения ID текущего пользователя

        if (duration != null && cycleLength != null && selectedDate.isNotEmpty()) {
            databaseHelper.insertCycleSettings(currentUserId, duration, cycleLength, selectedDate)
            Toast.makeText(this, "Настройки сохранены", Toast.LENGTH_SHORT).show()
            finish() // Завершение активности после сохранения
        } else {
            Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
        finish()
    }
}