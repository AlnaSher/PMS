package com.example.myapplication.activities

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.myapplication.R
import com.example.myapplication.database.DatabaseHelper

class CommonSettingsUpdateActivity : ComponentActivity() {
    private lateinit var nameEditText: EditText
    private lateinit var loginEditText: EditText
    private lateinit var newPasswordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val isTablet = resources.getBoolean(R.bool.is_tablet)
        if (!isTablet) {
            // Если это телефон, фиксируем ориентацию в портретной
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        setContentView(R.layout.activity_common_settings_update) // Убедитесь, что имя XML файла правильное

        // Инициализация элементов интерфейса
        nameEditText = findViewById(R.id.editTextText3)
        loginEditText = findViewById(R.id.editTextText4)
        newPasswordEditText = findViewById(R.id.editTextTextPassword4)
        confirmPasswordEditText = findViewById(R.id.editTextTextPassword5)
        saveButton = findViewById(R.id.button2)

        val userId = intent.getLongExtra("user_id", -1) // Получаем ID пользователя из Intent

        // Заполняем поля данными из базы данных
        fillUserData(userId)

        // Установка слушателя на кнопку "Сохранить изменения"
        saveButton.setOnClickListener {
            saveChanges(userId)
        }
    }

    private fun fillUserData(userId: Long) {
        val databaseHelper = DatabaseHelper(this)

        // Получаем данные по отдельности
        val name = databaseHelper.getUserNameById(userId)
        val login = databaseHelper.getUserLoginById(userId)
        val password = databaseHelper.getUserPasswordById(userId)

        // Заполнение полей
        nameEditText.setText(name ?: "")
        loginEditText.setText(login ?: "")
        newPasswordEditText.setText(password ?: "")
        confirmPasswordEditText.setText(password ?: "")
    }

    private fun saveChanges(userId: Long) {
        // Получение данных из EditText и Switch
        val name = nameEditText.text.toString()
        val login = loginEditText.text.toString()
        val newPassword = newPasswordEditText.text.toString()
        val confirmPassword = confirmPasswordEditText.text.toString()

        // Проверка, что новый пароль и подтверждение совпадают
        if (newPassword != confirmPassword) {
            Toast.makeText(this, "Пароли не совпадают", Toast.LENGTH_SHORT).show()
            return
        }

        // Обновление данных пользователя в базе данных
        val databaseHelper = DatabaseHelper(this)
        val success = databaseHelper.updateUser(
            userId,
            name,
            login,
            newPassword // Передаем новый пароль
        )

        if (success) {
            Toast.makeText(this, "Данные успешно обновлены", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, CommonSettingsViewActivity::class.java)
            intent.putExtra("user_id", userId)
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "Ошибка при обновлении данных", Toast.LENGTH_SHORT).show()
        }
    }
}