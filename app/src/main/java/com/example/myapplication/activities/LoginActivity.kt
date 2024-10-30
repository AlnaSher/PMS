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

class LoginActivity : ComponentActivity() {

    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val isTablet = resources.getBoolean(R.bool.is_tablet)
        if (!isTablet) {
            // Если это телефон, фиксируем ориентацию в портретной
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        setContentView(R.layout.activity_login)

        // Инициализация базы данных
        databaseHelper = DatabaseHelper(this)

        val buttonReg = findViewById<Button>(R.id.buttonReg)
        val buttonEnter = findViewById<Button>(R.id.buttonEnter)
        val editTextLogin = findViewById<EditText>(R.id.editTextLogin)
        val editTextPassword = findViewById<EditText>(R.id.editTextPassword)

        // Обработка нажатия кнопки "Регистрация"
        buttonReg.setOnClickListener {
            val intent = Intent(this, RegistrationActivity::class.java)
            startActivity(intent)
            finish()
        }

        buttonEnter.setOnClickListener {
            val login = editTextLogin.text.toString()
            val password = editTextPassword.text.toString()

            if (login.isNotEmpty() && password.isNotEmpty()) {
                // Проверка логина и пароля
                val userId = databaseHelper.getUserIdByLoginAndPassword(login, password)

                if (userId != -1L) {
                    // Сохранение ID пользователя в SharedPreferences
                    val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putLong("user_id", userId)
                    editor.apply()

                    // Переход на основную активность
                    val intent = Intent(this, ProfileActivity::class.java)
                    intent.putExtra("user_id", userId) // Передача ID
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Неверный логин или пароль", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Введите логин и пароль", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

