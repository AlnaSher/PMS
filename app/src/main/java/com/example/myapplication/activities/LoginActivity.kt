package com.example.myapplication.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.myapplication.R
import com.example.myapplication.database.DatabaseHelper
import java.security.KeyStore.TrustedCertificateEntry

class LoginActivity : ComponentActivity() {
    private lateinit var editTextLogin: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login) // Убедитесь, что у вас есть соответствующий XML файл

        val buttonReg = findViewById<Button>(R.id.buttonReg)
        val buttonLogin = findViewById<Button>(R.id.buttonEnter)
        editTextLogin = findViewById(R.id.editTextLogin)
        editTextPassword = findViewById(R.id.editTextPassword)

        dbHelper = DatabaseHelper(this) // Инициализация DatabaseHelper

        // Обработка нажатия кнопки "Войти"
        buttonLogin.setOnClickListener {
            authenticateUser()
        }

        // Обработка нажатия кнопки "Регистрация"
        buttonReg.setOnClickListener {
            val intent = Intent(this, RegistrationActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun authenticateUser() {
        val login = editTextLogin.text.toString().trim()
        val password = editTextPassword.text.toString().trim()

        // Проверка логина и пароля
        if (login.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }

        // Проверка в базе данных


        if (/*isAuthenticated*/login.isNotEmpty()) {
            // Если логин и пароль верны, переходим на экран профиля
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
            finish() // Закрываем LoginActivity
        } else {
            // Обработка неверного логина/пароля
            editTextPassword.error = "Неверный логин или пароль"
            Toast.makeText(this, "Неверный логин или пароль", Toast.LENGTH_SHORT).show()
        }
    }
}