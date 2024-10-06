package com.example.myapplication.database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "cycle_database"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "cycle_settings"
        private const val COLUMN_ID = "id"
        private const val COLUMN_USER_ID = "user_id"
        private const val COLUMN_DURATION = "duration"
        private const val COLUMN_CYCLE_LENGTH = "cycle_length"
        private const val COLUMN_DATE = "date"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = ("CREATE TABLE $TABLE_NAME (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_USER_ID INTEGER," +
                "$COLUMN_DURATION INTEGER," +
                "$COLUMN_CYCLE_LENGTH INTEGER," +
                "$COLUMN_DATE TEXT" +
                ")")
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    // Создание записи
    fun insertCycleSettings(userId: Int, duration: Int, cycleLength: Int, date: String): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USER_ID, userId)
            put(COLUMN_DURATION, duration)
            put(COLUMN_CYCLE_LENGTH, cycleLength)
            put(COLUMN_DATE, date)
        }
        val id = db.insert(TABLE_NAME, null, values)
        db.close()
        return id // Возвращаем ID вставленной записи
    }

    // Чтение всех записей по ID пользователя
    @SuppressLint("Range")
    fun getCycleSettingsByUserId(userId: Int): List<CycleSetting> {
        val cycleSettings = mutableListOf<CycleSetting>()
        val db = this.readableDatabase
        val cursor: Cursor = db.query(
            TABLE_NAME,
            null,
            "$COLUMN_USER_ID = ?",
            arrayOf(userId.toString()),
            null,
            null,
            null
        )

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID))
                val duration = cursor.getInt(cursor.getColumnIndex(COLUMN_DURATION))
                val cycleLength = cursor.getInt(cursor.getColumnIndex(COLUMN_CYCLE_LENGTH))
                val date = cursor.getString(cursor.getColumnIndex(COLUMN_DATE))
                cycleSettings.add(CycleSetting(id, userId, duration, cycleLength, date))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return cycleSettings
    }

    // Обновление записи
    fun updateCycleSettings(id: Int, duration: Int, cycleLength: Int, date: String): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_DURATION, duration)
            put(COLUMN_CYCLE_LENGTH, cycleLength)
            put(COLUMN_DATE, date)
        }
        val rowsAffected = db.update(TABLE_NAME, values, "$COLUMN_ID = ?", arrayOf(id.toString()))
        db.close()
        return rowsAffected // Возвращаем количество затронутых строк
    }

    // Удаление записи
    fun deleteCycleSettings(id: Int): Int {
        val db = this.writableDatabase
        val rowsDeleted = db.delete(TABLE_NAME, "$COLUMN_ID = ?", arrayOf(id.toString()))
        db.close()
        return rowsDeleted // Возвращаем количество удаленных строк
    }

    // Получение всех настроек циклов
    @SuppressLint("Range")
    fun getAllCycleSettings(): List<CycleSetting> {
        val cycleSettings = mutableListOf<CycleSetting>()
        val db = this.readableDatabase
        val cursor: Cursor = db.query(
            TABLE_NAME,
            null,
            null,
            null,
            null,
            null,
            null
        )

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID))
                val userId = cursor.getInt(cursor.getColumnIndex(COLUMN_USER_ID))
                val duration = cursor.getInt(cursor.getColumnIndex(COLUMN_DURATION))
                val cycleLength = cursor.getInt(cursor.getColumnIndex(COLUMN_CYCLE_LENGTH))
                val date = cursor.getString(cursor.getColumnIndex(COLUMN_DATE))
                cycleSettings.add(CycleSetting(id, userId, duration, cycleLength, date))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return cycleSettings
    }

    fun getAllData() {

    }
}

// Класс для представления настроек цикла
data class CycleSetting(
    val id: Int,
    val userId: Int,
    val duration: Int,
    val cycleLength: Int,
    val date: String
)