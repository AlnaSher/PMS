package com.example.myapplication.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.myapplication.R
import com.example.myapplication.database.DatabaseHelper
import com.example.myapplication.helperClasses.UserSession

class RegistrationActivity : ComponentActivity() {
    private lateinit var editTextName: EditText
    private lateinit var editTextLogin: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var editTextPasswordConfirm: EditText
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        val buttonLogin = findViewById<Button>(R.id.buttonAuth)

        editTextName = findViewById(R.id.editTextText) // Имя
        editTextLogin = findViewById(R.id.editTextLoginRegistr) // Логин
        editTextPassword = findViewById(R.id.editTextPasswordReg) // Пароль
        editTextPasswordConfirm = findViewById(R.id.editTextPasswordRegConfirm) // Подтверждение пароля

        val buttonRegister = findViewById<Button>(R.id.buttonRegister)
        dbHelper = DatabaseHelper(this) // Инициализация DatabaseHelper

        buttonRegister.setOnClickListener {
            registerUser()
        }

        // Обработка нажатия кнопки "Авторизация"
        buttonLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun registerUser() {
        val name = editTextName.text.toString().trim()
        val login = editTextLogin.text.toString().trim()
        val password = editTextPassword.text.toString().trim()
        val passwordConfirm = editTextPasswordConfirm.text.toString().trim()

        // Проверка на пустые поля
        if (name.isEmpty() || login.isEmpty() || password.isEmpty() || passwordConfirm.isEmpty()) {
            Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }

        // Проверка совпадения паролей
        if (password != passwordConfirm) {
            Toast.makeText(this, "Пароли не совпадают", Toast.LENGTH_SHORT).show()
            return
        }

       /* // Проверка на существующий логин
        val cursor = dbHelper.getAllData()
        while (cursor.moveToNext()) {
            val existingLogin = cursor.getString(cursor.getColumnIndexOrThrow("login"))
            if (existingLogin == login) {
                Toast.makeText(this, "Пользователь с таким логином уже существует", Toast.LENGTH_SHORT).show()
                cursor.close()
                return
            }
        }
        cursor.close()

        // Сохранение данных в базе данных
        val result = dbHelper.insertData(name, login, password)

        if (result != -1L) {
            // Сохранение ID пользователя в UserSession
            UserSession.setUserId(result.toInt()) // Предполагая, что result - это ID пользователя

            Toast.makeText(this, "Регистрация успешна!", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, CycleSettingsActivity::class.java)
            startActivity(intent)
            finish() // Закрываем RegistrationActivity, чтобы пользователь не мог вернуться назад
        } else {
            Toast.makeText(this, "Ошибка регистрации, попробуйте снова", Toast.LENGTH_SHORT).show()
        }*/
        if (/*isAuthenticated*/login.isNotEmpty() && password.isNotEmpty()) {
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