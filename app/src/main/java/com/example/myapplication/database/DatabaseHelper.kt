package com.example.myapplication.database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.myapplication.helperClasses.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "users.db"
        private const val DATABASE_VERSION = 10 // Обновляем версию для миграции

        // Таблицы и столбцы
        private const val TABLE_USERS = "users"
        private const val COLUMN_ID = "id"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_LOGIN = "login"
        private const val COLUMN_PASSWORD = "password"
        private const val COLUMN_CYCLE_START_ENABLED = "cycle_start_enabled"
        private const val COLUMN_OVULATION_ENABLED = "ovulation_enabled"
        private const val COLUMN_PERIOD_ENABLED = "period_enabled"
        private const val COLUMN_REMEMBER_ME_ENABLED = "remember_me_enabled"

        // Таблица для настроек цикла
        private const val TABLE_CYCLE_SETTINGS = "cycle_settings"
        private const val COLUMN_CYCLE_LENGTH = "cycle_length"
        private const val COLUMN_DURATION = "duration"
        private const val COLUMN_LAST_PERIOD_DATE = "last_period_date"

        // Таблица для статусов дня
        private const val TABLE_DAY_STATUS = "day_status"
        private const val COLUMN_DATE = "date"
        private const val COLUMN_DAY_STATUS = "status"
        private const val COLUMN_USER_ID = "user_id"
        private const val COLUMN_DAY_OF_CYCLE = "day_of_cycle" // Новый столбец для дня цикла
        private const val COLUMN_SYMPTOMS = "symptoms" // Новый столбец для симптомов

        // Таблица для симптомов
        private const val TABLE_SYMPTOMS = "symptoms"
        private const val COLUMN_MOOD = "mood"
        private const val COLUMN_VAGINAL_DISCHARGE = "vaginal_discharge"
        private const val COLUMN_PHYSICAL_ACTIVITY = "physical_activity"
        private const val COLUMN_BODY_PAIN = "body_pain"
        private const val COLUMN_SKIN_CONDITION = "skin_condition"

        // Таблица для циклов
        private const val TABLE_CYCLES = "cycles"
        private const val COLUMN_CYCLES_ID = "id"
        private const val COLUMN_START_DATE = "startDate"
        private const val COLUMN_END_DATE = "endDate"
        private const val COLUMN_LENGTH = "length"
        private const val COLUMN_CYCLES_SYMPTOMS = "symptoms"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Создание таблиц
        val createTableUsers = """
        CREATE TABLE $TABLE_USERS (
            $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_NAME TEXT,
            $COLUMN_LOGIN TEXT UNIQUE,
            $COLUMN_PASSWORD TEXT,
            $COLUMN_CYCLE_START_ENABLED INTEGER DEFAULT 0,
            $COLUMN_OVULATION_ENABLED INTEGER DEFAULT 0,
            $COLUMN_PERIOD_ENABLED INTEGER DEFAULT 0,
            $COLUMN_REMEMBER_ME_ENABLED INTEGER DEFAULT 0
        )
        """.trimIndent()

        val createCycleSettingsTable = """
        CREATE TABLE $TABLE_CYCLE_SETTINGS (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            user_id INTEGER,
            $COLUMN_CYCLE_LENGTH INTEGER,
            $COLUMN_DURATION INTEGER,
            $COLUMN_LAST_PERIOD_DATE TEXT,
            FOREIGN KEY(user_id) REFERENCES $TABLE_USERS($COLUMN_ID)
        )
        """.trimIndent()

        val createDayStatusTable = """
        CREATE TABLE $TABLE_DAY_STATUS (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_USER_ID INTEGER,
            $COLUMN_DATE TEXT,
            $COLUMN_DAY_STATUS TEXT,
            $COLUMN_DAY_OF_CYCLE INTEGER, -- Добавлен столбец для дня цикла
            $COLUMN_SYMPTOMS TEXT, -- Добавлен столбец для симптомов
            FOREIGN KEY($COLUMN_USER_ID) REFERENCES $TABLE_USERS($COLUMN_ID)
        )
        """.trimIndent()

        val createSymptomsTable = """
        CREATE TABLE $TABLE_SYMPTOMS (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_USER_ID INTEGER,
            $COLUMN_DATE TEXT,
            $COLUMN_MOOD TEXT,
            $COLUMN_VAGINAL_DISCHARGE TEXT,
            $COLUMN_PHYSICAL_ACTIVITY TEXT,
            $COLUMN_BODY_PAIN TEXT,
            $COLUMN_SKIN_CONDITION TEXT,
            FOREIGN KEY($COLUMN_USER_ID) REFERENCES $TABLE_USERS($COLUMN_ID),
            FOREIGN KEY($COLUMN_DATE) REFERENCES $TABLE_DAY_STATUS($COLUMN_DATE)
        )
        """.trimIndent()

        val createCyclesTable = """
        CREATE TABLE IF NOT EXISTS $TABLE_CYCLES (
            $COLUMN_CYCLES_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_USER_ID INTEGER NOT NULL, -- новый столбец для идентификатора пользователя
            $COLUMN_START_DATE TEXT NOT NULL,
            $COLUMN_END_DATE TEXT NOT NULL,
            $COLUMN_LENGTH INTEGER NOT NULL,
            $COLUMN_CYCLES_SYMPTOMS TEXT,
            FOREIGN KEY($COLUMN_USER_ID) REFERENCES $TABLE_USERS($COLUMN_ID) -- связь с таблицей пользователей
        )
        """.trimIndent()

        db.execSQL(createTableUsers)
        db.execSQL(createCycleSettingsTable)
        db.execSQL(createDayStatusTable)
        db.execSQL(createSymptomsTable)
        db.execSQL(createCyclesTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CYCLE_SETTINGS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_DAY_STATUS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SYMPTOMS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CYCLES")
        onCreate(db)
    }


    // Методы работы с пользователями

    fun addUser(name: String, login: String, password: String): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_NAME, name)
            put(COLUMN_LOGIN, login)
            put(COLUMN_PASSWORD, password)
            put(COLUMN_CYCLE_START_ENABLED, 0)
            put(COLUMN_OVULATION_ENABLED, 0)
            put(COLUMN_PERIOD_ENABLED, 0)
            put(COLUMN_REMEMBER_ME_ENABLED, 0)
        }
        return db.insert(TABLE_USERS, null, contentValues)
    }

    fun addOrUpdateCycleSettings(userId: Long, cycleLength: Int, duration: Int, lastPeriodDate: String): Long {
        val db = this.writableDatabase

        // Проверяем, существует ли запись для данного userId
        val query = "SELECT * FROM $TABLE_CYCLE_SETTINGS WHERE user_id = ?"
        val cursor = db.rawQuery(query, arrayOf(userId.toString()))

        return if (cursor.moveToFirst()) {
            // Если запись существует, обновляем ее
            val contentValues = ContentValues().apply {
                put(COLUMN_CYCLE_LENGTH, cycleLength)
                put(COLUMN_DURATION, duration)
                put(COLUMN_LAST_PERIOD_DATE, lastPeriodDate)
            }

            // Обновляем запись по user_id
            val rowsAffected = db.update(TABLE_CYCLE_SETTINGS, contentValues, "user_id = ?", arrayOf(userId.toString()))
            cursor.close()
            rowsAffected.toLong() // Возвращаем количество затронутых строк (или ID обновленной записи)
        } else {
            // Если записи не существует, создаем новую
            val contentValues = ContentValues().apply {
                put("user_id", userId)
                put(COLUMN_CYCLE_LENGTH, cycleLength)
                put(COLUMN_DURATION, duration)
                put(COLUMN_LAST_PERIOD_DATE, lastPeriodDate)
            }

            val id = db.insert(TABLE_CYCLE_SETTINGS, null, contentValues)
            cursor.close()
            id // Возвращаем ID новой записи
        }
    }


    fun insertCycle(cycle: Cycle, userId: Long) {
        val db = this.writableDatabase // Получаем доступ к базе данных для записи

        // Создаем объект ContentValues для хранения значений
        val values = ContentValues().apply {
            put(COLUMN_USER_ID, userId)                        // Добавляем идентификатор пользователя
            put(COLUMN_START_DATE, cycle.startDate)           // Начальная дата
            put(COLUMN_END_DATE, cycle.endDate)               // Конечная дата
            put(COLUMN_LENGTH, cycle.length)                   // Длина цикла
            put(COLUMN_CYCLES_SYMPTOMS, cycle.symptoms)      // Симптомы
        }

        // Вставляем новую запись в таблицу циклов
        val result = db.insert(TABLE_CYCLES, null, values)

        if (result == -1L) {
            // Ошибка при вставке
            Log.e("Database", "Failed to insert cycle")
        } else {
            Log.d("Database", "Cycle inserted successfully with ID: $result")
        }

        db.close() // Закрываем базу данных
    }



    fun updateUser(userId: Long, name: String, login: String, password: String): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_NAME, name)
            put(COLUMN_LOGIN, login)
            put(COLUMN_PASSWORD, password)
        }
        return db.update(TABLE_USERS, contentValues, "$COLUMN_ID = ?", arrayOf(userId.toString())) > 0
    }




    @RequiresApi(Build.VERSION_CODES.O)
    fun isDayMarkedAsMenstruation(userId: Long, date: LocalDate): Boolean {
        val db = this.readableDatabase

        val status = getDayStatus(userId, date)
        return status == "Менструация"
    }








    fun isCycleStartEnabled(userId: Long): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT $COLUMN_CYCLE_START_ENABLED FROM $TABLE_USERS WHERE $COLUMN_ID = ?", arrayOf(userId.toString()))

        return if (cursor.moveToFirst()) {
            val isEnabled = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CYCLE_START_ENABLED)) == 1
            cursor.close()
            isEnabled
        } else {
            cursor.close()
            false
        }
    }

    // Метод для проверки, включена ли "Овуляция"
    fun isOvulationEnabled(userId: Long): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT $COLUMN_OVULATION_ENABLED FROM $TABLE_USERS WHERE $COLUMN_ID = ?", arrayOf(userId.toString()))

        return if (cursor.moveToFirst()) {
            val isEnabled = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_OVULATION_ENABLED)) == 1
            cursor.close()
            isEnabled
        } else {
            cursor.close()
            false
        }
    }

    // Метод для проверки, включены ли "Месячные"
    fun isPeriodEnabled(userId: Long): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT $COLUMN_PERIOD_ENABLED FROM $TABLE_USERS WHERE $COLUMN_ID = ?", arrayOf(userId.toString()))

        return if (cursor.moveToFirst()) {
            val isEnabled = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PERIOD_ENABLED)) == 1
            cursor.close()
            isEnabled
        } else {
            cursor.close()
            false
        }
    }

    // Метод для проверки, включена ли опция "Запомнить меня"
    fun isRememberMeEnabled(userId: Long): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT $COLUMN_REMEMBER_ME_ENABLED FROM $TABLE_USERS WHERE $COLUMN_ID = ?", arrayOf(userId.toString()))

        return if (cursor.moveToFirst()) {
            val isEnabled = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_REMEMBER_ME_ENABLED)) == 1
            cursor.close()
            isEnabled
        } else {
            cursor.close()
            false
        }
    }
    @SuppressLint("Range")
    fun getAllCycles(userId: Long): List<Cycle> {
        val cyclesList = mutableListOf<Cycle>()
        val db = this.readableDatabase

        // Изменяем SQL-запрос, чтобы получать только циклы для указанного пользователя
        val cursor = db.rawQuery("SELECT * FROM $TABLE_CYCLES WHERE user_id = ?", arrayOf(userId.toString()))

        // Проверяем, есть ли данные
        if (cursor.moveToFirst()) {
            do {
                // Получаем данные из курсора
                val id = cursor.getLong(cursor.getColumnIndex(COLUMN_CYCLES_ID))
                val startDate = cursor.getString(cursor.getColumnIndex(COLUMN_START_DATE))
                val endDate = cursor.getString(cursor.getColumnIndex(COLUMN_END_DATE))
                val length = cursor.getInt(cursor.getColumnIndex(COLUMN_LENGTH))
                val symptoms = cursor.getString(cursor.getColumnIndex(COLUMN_CYCLES_SYMPTOMS))

                // Создаем объект Cycle и добавляем его в список
                val cycle = Cycle(id, startDate, endDate, length, symptoms)
                cyclesList.add(cycle)
            } while (cursor.moveToNext())
        }

        // Закрываем курсор и базу данных
        cursor.close()
        db.close()

        return cyclesList
    }

    fun getUserIdByLoginAndPassword(login: String, password: String): Long {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COLUMN_ID FROM $TABLE_USERS WHERE $COLUMN_LOGIN = ? AND $COLUMN_PASSWORD = ?",
            arrayOf(login, password)
        )
        return if (cursor.moveToFirst()) {
            cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID))
        } else {
            -1
        }.also { cursor.close() }
    }

    fun getUserNameById(userId: Long): String? {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT $COLUMN_NAME FROM $TABLE_USERS WHERE $COLUMN_ID = ?", arrayOf(userId.toString()))
        return if (cursor.moveToFirst()) {
            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
        } else {
            null
        }.also { cursor.close() }
    }

    fun getUserLoginById(userId: Long): String? {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT $COLUMN_LOGIN FROM $TABLE_USERS WHERE $COLUMN_ID = ?", arrayOf(userId.toString()))
        return if (cursor.moveToFirst()) {
            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOGIN))
        } else {
            null
        }.also { cursor.close() }
    }

    fun getUserPasswordById(userId: Long): String? {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT $COLUMN_PASSWORD FROM $TABLE_USERS WHERE $COLUMN_ID = ?", arrayOf(userId.toString()))
        return if (cursor.moveToFirst()) {
            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD))
        } else {
            null
        }.also { cursor.close() }
    }

    @SuppressLint("Range")
    fun getSymptomsInCycle(userId: Long, startDate: String, endDate: String): List<String> {
        val symptomsSet = mutableSetOf<String>() // Множество для хранения уникальных симптомов
        val db = this.readableDatabase

        val query = """
        SELECT DISTINCT $COLUMN_MOOD, $COLUMN_VAGINAL_DISCHARGE, $COLUMN_PHYSICAL_ACTIVITY, 
                        $COLUMN_BODY_PAIN, $COLUMN_SKIN_CONDITION 
        FROM $TABLE_SYMPTOMS
        WHERE $COLUMN_USER_ID = ? 
        AND $COLUMN_DATE BETWEEN ? AND ?
    """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(userId.toString(), startDate, endDate))

        if (cursor.moveToFirst()) {
            do {
                // Проверяем каждый столбец на наличие симптомов и добавляем их в set, если они не пусты
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MOOD))?.let {
                    if (it.isNotEmpty()) symptomsSet.add(it)
                }
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VAGINAL_DISCHARGE))?.let {
                    if (it.isNotEmpty()) symptomsSet.add(it)
                }
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHYSICAL_ACTIVITY))?.let {
                    if (it.isNotEmpty()) symptomsSet.add(it)
                }
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BODY_PAIN))?.let {
                    if (it.isNotEmpty()) symptomsSet.add(it)
                }
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SKIN_CONDITION))?.let {
                    if (it.isNotEmpty()) symptomsSet.add(it)
                }
            } while (cursor.moveToNext())
        }

        cursor.close()
        return symptomsSet.toList() // Возвращаем список уникальных симптомов
    }

    fun isDayMarkedAsOvulation(userId: Long, date: String): Boolean {
        val db = this.readableDatabase
        var isMarked = false

        // Обновленный запрос с использованием корректного имени столбца user_id
        val query = "SELECT COUNT(*) FROM $TABLE_DAY_STATUS WHERE $COLUMN_USER_ID = ? AND $COLUMN_DATE = ? AND $COLUMN_DAY_STATUS = ?"
        val cursor = db.rawQuery(query, arrayOf(userId.toString(), date, DayStatus.OVULATION.description))

        if (cursor.moveToFirst()) {
            isMarked = cursor.getInt(0) > 0 // Если количество больше 0, значит день отмечен
        }
        cursor.close()
        db.close()
        return isMarked
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getLastOvulationDate(userId: Long): LocalDate? {
        val db = this.readableDatabase
        val query = """
        SELECT date FROM day_status
        WHERE user_id = ? AND status = ?
        ORDER BY date DESC
        LIMIT 1
    """
        val cursor = db.rawQuery(query, arrayOf(userId.toString(), "Овуляция"))

        return if (cursor.moveToFirst()) {
            val dateString = cursor.getString(cursor.getColumnIndexOrThrow("date"))
            LocalDate.parse(dateString, DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        } else {
            null
        }.also {
            cursor.close()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getLastPeriodStartDate(userId: Long): LocalDate? {
        val cycleSettings = getCycleSettings(userId);
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        return  LocalDate.parse(cycleSettings?.get("lastPeriodDate") as String, formatter)
    }


    // Методы работы с симптомами

    fun addSymptoms(userId: Long, date: LocalDate, symptoms: Symptoms): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_USER_ID, userId)
            put(COLUMN_DATE, date.toString())
            put(COLUMN_MOOD, symptoms.mood.joinToString(",") { it.toString() })
            put(COLUMN_VAGINAL_DISCHARGE, symptoms.vaginalDischarge.joinToString(",") { it.toString() })
            put(COLUMN_PHYSICAL_ACTIVITY, symptoms.physicalActivity.toString())
            put(COLUMN_BODY_PAIN, symptoms.bodyPain.joinToString(",") { it.toString() })
            put(COLUMN_SKIN_CONDITION, symptoms.skinCondition.joinToString(",") { it.toString() })
        }
        return db.insert(TABLE_SYMPTOMS, null, contentValues)
    }

    fun getSymptoms(userId: Long, date: LocalDate): Symptoms? {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_SYMPTOMS WHERE $COLUMN_USER_ID = ? AND $COLUMN_DATE = ?", arrayOf(userId.toString(), date.toString()))

        return if (cursor.moveToFirst()) {
            val mood = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MOOD)).split(",").mapNotNull { MoodSymptoms.valueOf(it) }
            val vaginalDischarge = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VAGINAL_DISCHARGE)).split(",").mapNotNull { VaginalDischargeSymptoms.valueOf(it) }
            val physicalActivity = PhysicalActivitySymptoms.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHYSICAL_ACTIVITY)))
            val bodyPain = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BODY_PAIN)).split(",").mapNotNull { BodyPainSymptoms.valueOf(it) }
            val skinCondition = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SKIN_CONDITION)).split(",").mapNotNull { SkinConditionSymptoms.valueOf(it) }

            cursor.close()
            Symptoms(mood, vaginalDischarge, physicalActivity, bodyPain, skinCondition)
        } else {
            cursor.close()
            null
        }
    }

    // Методы работы с настройками цикла

    fun getCycleSettings(userId: Long): Map<String, Any?>? {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_CYCLE_SETTINGS WHERE $COLUMN_USER_ID = ?", arrayOf(userId.toString()))

        return if (cursor.moveToFirst()) {
            val cycleLength = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CYCLE_LENGTH))
            val duration = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DURATION))
            val lastPeriodDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LAST_PERIOD_DATE))
            cursor.close()
            mapOf(
                "cycleLength" to cycleLength,
                "duration" to duration,
                "lastPeriodDate" to lastPeriodDate
            )
        } else {
            cursor.close()
            null
        }
    }

    @SuppressLint("Range")
    fun getMenstrualDays(userId: Long): List<String> {
        val db = this.readableDatabase
        val menstrualDays = mutableListOf<String>()
        val cursor = db.query(
            TABLE_DAY_STATUS,
            arrayOf(COLUMN_DATE),
            "$COLUMN_USER_ID = ? AND $COLUMN_DAY_STATUS = ?",
            arrayOf(userId.toString(), "Менструация"),
            null, null, null
        )

        if (cursor != null && cursor.moveToFirst()) {
            do {
                val date = cursor.getString(cursor.getColumnIndex(COLUMN_DATE))
                menstrualDays.add(date)
            } while (cursor.moveToNext())
        }
        cursor?.close()
        return menstrualDays
    }


    // Методы для обновления флагов настроек
    fun updateCycleStartEnabled(userId: Long, isEnabled: Boolean): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_CYCLE_START_ENABLED, if (isEnabled) 1 else 0)
        }
        val rowsAffected = db.update(TABLE_USERS, contentValues, "$COLUMN_ID = ?", arrayOf(userId.toString()))
        return rowsAffected > 0
    }

    fun updateOvulationEnabled(userId: Long, isEnabled: Boolean): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_OVULATION_ENABLED, if (isEnabled) 1 else 0)
        }
        val rowsAffected = db.update(TABLE_USERS, contentValues, "$COLUMN_ID = ?", arrayOf(userId.toString()))
        return rowsAffected > 0
    }

    fun updatePeriodEnabled(userId: Long, isEnabled: Boolean): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_PERIOD_ENABLED, if (isEnabled) 1 else 0)
        }
        val rowsAffected = db.update(TABLE_USERS, contentValues, "$COLUMN_ID = ?", arrayOf(userId.toString()))
        return rowsAffected > 0
    }

    fun updateRememberMeEnabled(userId: Long, isEnabled: Boolean): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_REMEMBER_ME_ENABLED, if (isEnabled) 1 else 0)
        }
        val rowsAffected = db.update(TABLE_USERS, contentValues, "$COLUMN_ID = ?", arrayOf(userId.toString()))
        return rowsAffected > 0
    }

    // Пример методов для статусов дня

    fun addOrUpdateDayStatus(userId: Long, date: LocalDate, status: String, dayOfCycle: Int, symptoms: List<String>? = null): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_USER_ID, userId)
            put(COLUMN_DATE, date.toString())
            put(COLUMN_DAY_STATUS, status)
            put(COLUMN_DAY_OF_CYCLE, dayOfCycle)
            symptoms?.let {
                if (it.isNotEmpty()) {
                    put(COLUMN_SYMPTOMS, it.joinToString(","))
                }
            }
        }

        // Проверяем, существует ли запись для указанного userId и date
        val existingCursor = db.rawQuery(
            "SELECT $COLUMN_DAY_STATUS FROM $TABLE_DAY_STATUS WHERE $COLUMN_USER_ID = ? AND $COLUMN_DATE = ?",
            arrayOf(userId.toString(), date.toString())
        )

        val result: Long = if (existingCursor.moveToFirst()) {
            // Если запись существует, обновляем её
            db.update(
                TABLE_DAY_STATUS, contentValues,
                "$COLUMN_USER_ID = ? AND $COLUMN_DATE = ?",
                arrayOf(userId.toString(), date.toString())
            ).toLong()
        } else {
            // Вставляем новую запись
            db.insert(TABLE_DAY_STATUS, null, contentValues)
        }

        existingCursor.close()
        db.close() // Закрываем базу данных только после завершения всех операций
        return result
    }


    @SuppressLint("Range")
    fun getCycleHistory(userId: Long): List<Cycle> {
        val cycles = mutableListOf<Cycle>()
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_CYCLES WHERE user_id = ? ORDER BY $COLUMN_START_DATE DESC",
            arrayOf(userId.toString()) // Передаем userId как параметр
        )
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndex(COLUMN_CYCLES_ID)) // Используйте getLong
                val startDate = cursor.getString(cursor.getColumnIndex(COLUMN_START_DATE))
                val endDate = cursor.getString(cursor.getColumnIndex(COLUMN_END_DATE))
                val length = cursor.getInt(cursor.getColumnIndex(COLUMN_LENGTH))
                val symptoms = cursor.getString(cursor.getColumnIndex(COLUMN_CYCLES_SYMPTOMS))

                val cycle = Cycle(id, startDate, endDate, length, symptoms)
                cycles.add(cycle)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()

        return cycles
    }


    fun getDayStatus(userId: Long, date: LocalDate): String? {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT $COLUMN_DAY_STATUS FROM $TABLE_DAY_STATUS WHERE $COLUMN_USER_ID = ? AND $COLUMN_DATE = ?", arrayOf(userId.toString(), date.toString()))
        return if (cursor.moveToFirst()) {
            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DAY_STATUS))
        } else {
            null
        }.also { cursor.close() }
    }
}
