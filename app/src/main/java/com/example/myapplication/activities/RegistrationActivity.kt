package com.example.myapplication.activities

import com.example.myapplication.database.DatabaseHelper
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.myapplication.R

class RegistrationActivity : ComponentActivity() {

    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val isTablet = resources.getBoolean(R.bool.is_tablet)
        if (!isTablet) {
            // Если это телефон, фиксируем ориентацию в портретной
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        setContentView(R.layout.activity_registration)

        // Инициализация базы данных
        databaseHelper = DatabaseHelper(this)

        val buttonLogin = findViewById<Button>(R.id.buttonAuth)
        val buttonRegister = findViewById<Button>(R.id.buttonRegister)
        val editTextName = findViewById<EditText>(R.id.editTextText)
        val editTextLogin = findViewById<EditText>(R.id.editTextLoginRegistr)
        val editTextPassword = findViewById<EditText>(R.id.editTextPasswordReg)

        // Обработка нажатия кнопки "Авторизация"
        buttonLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        buttonRegister.setOnClickListener {
            val name = editTextName.text.toString()
            val login = editTextLogin.text.toString()
            val password = editTextPassword.text.toString()

            if (name.isNotEmpty() && login.isNotEmpty() && password.isNotEmpty()) {
                // Добавление пользователя в базу данных
                val userId = databaseHelper.addUser(name, login, password)

                if (userId != -1L) {
                    Toast.makeText(this, "Регистрация успешна", Toast.LENGTH_SHORT).show()

                    // Сохранение ID пользователя в SharedPreferences
                    val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putLong("user_id", userId)
                    editor.apply()

                    // Переход на следующую активность
                    val intent = Intent(this, CycleSettingsActivity::class.java)
                    intent.putExtra("user_id", userId) // Передача ID
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Ошибка регистрации", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Все поля должны быть заполнены", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

